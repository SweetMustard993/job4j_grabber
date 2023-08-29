CREATE TABLE company
(
    id integer NOT NULL,
    name character varying,
    CONSTRAINT company_pkey PRIMARY KEY (id)
);

CREATE TABLE person
(
    id integer NOT NULL,
    name character varying,
    company_id integer references company(id),
    CONSTRAINT person_pkey PRIMARY KEY (id)
);


insert into company values (1, 'bb');
insert into company values (2, 'aa');
insert into company values (5, 'cc');

insert into person values (1, 'arvin', 1);
insert into person values (2, 'aron', 1);
insert into person values (3, 'mikasa', 2);
insert into person values (4, 'marvin', 5);
insert into person values (5, 'reinar', 2);
insert into person values (6, 'gobald', 5);
insert into person values (7, 'sandji', 5);
insert into person values (8, 'arlen', 1);

select c.name, p.name from person p
join company c on p.company_id=c.id
where c.id != 5;

select c.name, p.name from person p
join company c on p.company_id=c.id
group by c.name
having count(p.id);

select c.name, count(p.id) from person p
join company c on p.company_id=c.id
group by c.name
having count(p.id) = (select count(p.id) cn from person p
join company c on p.company_id=c.id
group by c.name
order by cn desc
limit 1);