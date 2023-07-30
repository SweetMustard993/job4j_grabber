package ru.job4j.quartz;

import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.quartz.JobBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;

public class AlertRabbit {
    public static void main(String[] args) {
        Connection connection = initConnection();
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            JobDataMap data = new JobDataMap();
            data.put("connection", connection);
            JobDetail job = newJob(Rabbit.class)
                    .usingJobData(data)
                    .build();
            SimpleScheduleBuilder times = simpleSchedule()
                    .withIntervalInSeconds(5)
                    .repeatForever();
            Trigger trigger = newTrigger()
                    .startNow()
                    .withSchedule(times)
                    .build();
            scheduler.scheduleJob(job, trigger);
            Thread.sleep(10000);
            scheduler.shutdown();
            System.out.println(connection);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private static Properties loadProperties() {
        Properties config = new Properties();
        try (InputStream in = new FileInputStream("src/main/resources/rabbit.properties")) {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

    private static Connection initConnection() {
        Properties config = loadProperties();
        Connection cn = null;
        try {
            Class.forName(config.getProperty("driver-class-name"));
            cn = DriverManager.getConnection(config.getProperty("url"),
                    config.getProperty("username"),
                    config.getProperty("password"));
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
        return cn;
    }

    public static class Rabbit implements Job {

        public Rabbit() {
            System.out.println(hashCode());
        }

        @Override
        public void execute(JobExecutionContext context) {
            System.out.println("Rabbit runs here ...");
            Connection connection = (Connection) context.getJobDetail().getJobDataMap().get("connection");
            try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO grabber.rabbit (created_date) VALUES (?)")) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                preparedStatement.execute();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}