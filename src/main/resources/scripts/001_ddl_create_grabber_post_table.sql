create table grabber.post(
id serial primary key,
name char(255),
text text,
link text UNIQUE,
created timestamp
);