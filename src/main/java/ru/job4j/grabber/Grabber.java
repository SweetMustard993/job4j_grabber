package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public class Grabber implements Grab {
    private static final String DB_STORE = "dbStore";
    private static final String HABR_CAREER_PARSE = "habrCareerParse";
    private static final String LINK = "https://career.habr.com/vacancies/java_developer?page=";
    private static final String PROPERTIES = "src/main/resources/grabber.properties";
    private static final String INTERVAL = "grabber.interval";

    private final Parse parse;
    private final Store store;
    private final Scheduler scheduler;
    private final int time;

    public Grabber(Parse parse, Store store, Scheduler scheduler, int time) {
        this.parse = parse;
        this.store = store;
        this.scheduler = scheduler;
        this.time = time;
    }

    private static Properties loadProperties() {
        Properties config = new Properties();
        try (InputStream in = new FileInputStream(PROPERTIES)) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    @Override
    public void init() {
        try {
            JobDataMap data = new JobDataMap();
            data.put(DB_STORE, store);
            data.put(HABR_CAREER_PARSE, parse);
            JobDetail job = newJob(GrabJob.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(time)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public void web(Store store) {
        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(Integer.parseInt(loadProperties().getProperty("grabber.port")))) {
                while (!server.isClosed()) {
                    Socket socket = server.accept();
                    try (OutputStream out = socket.getOutputStream()) {
                        out.write("HTTP/1.1 200 OK\r\n\r\n".getBytes());
                        for (Post post : store.getAll()) {
                            out.write(post.toString().getBytes(Charset.forName("Windows-1251")));
                            out.write(System.lineSeparator().getBytes());
                        }
                    } catch (IOException io) {
                        io.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static class GrabJob implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) {
            JobDataMap map = jobExecutionContext.getJobDetail().getJobDataMap();
            Store dbStore = (Store) map.get(DB_STORE);
            HabrCareerParse habrCareerParse = (HabrCareerParse) map.get(HABR_CAREER_PARSE);
            List<Post> posts = habrCareerParse.list(LINK);
            posts.forEach(dbStore::save);
        }
    }

    public static void main(String[] args) throws SchedulerException {
        var cfg = loadProperties();
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        scheduler.start();
        var parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        var store = new PsqlStore(cfg);
        var time = Integer.parseInt(cfg.getProperty("grabber.time"));
        Grabber grab = new Grabber(parse, store, scheduler, time);
        grab.init();
        grab.web(store);
    }
}
