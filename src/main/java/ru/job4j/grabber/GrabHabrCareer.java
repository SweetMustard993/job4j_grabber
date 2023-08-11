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

public class GrabHabrCareer implements Grab {
    private static final String DB_STORE = "dbStore";
    private static final String HABR_CAREER_PARSE = "habrCareerParse";
    private static final String LINK = "https://career.habr.com/vacancies/java_developer?page=";
    private static final String PROPERTIES = "src/main/resources/grabber.properties";
    private static final String INTERVAL = "grabber.interval";

    private final PsqlStore dbStore;
    private final HabrCareerParse habrCareerParse;

    public GrabHabrCareer(PsqlStore dbStore, HabrCareerParse habrCareerParse) {
        this.dbStore = dbStore;
        this.habrCareerParse = habrCareerParse;
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
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put(DB_STORE, dbStore);
            data.put(HABR_CAREER_PARSE, habrCareerParse);
            JobDetail job = newJob(Grabber.class)
                    .usingJobData(data)
                    .build();
            int interval = Integer.parseInt(loadProperties().getProperty(INTERVAL));
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(interval)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(1000000);
            scheduler.shutdown();
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    public static class Grabber implements Job {

        @Override
        public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
            System.out.println("Initialing grabber");
            System.out.println("Initialing connect to database");
            Store dbStore = (Store) jobExecutionContext.getJobDetail().getJobDataMap().get(DB_STORE);
            System.out.println("Star parse HabrCareer");
            HabrCareerParse habrCareerParse = (HabrCareerParse) jobExecutionContext.getJobDetail()
                    .getJobDataMap().get(HABR_CAREER_PARSE);
            System.out.println("Start Grabbing Habr");
            List<Post> posts = habrCareerParse.list(LINK);
            posts.forEach(dbStore::save);
        }
    }

    public static void main(String[] args) {
        Grab habr = new GrabHabrCareer(new PsqlStore(loadProperties()), new HabrCareerParse(new HabrCareerDateTimeParser()));
        try {
            habr.init();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
