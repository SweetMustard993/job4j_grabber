package ru.job4j.grabber;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
        new Grabber(parse, store, scheduler, time).init();
    }
}
