create table post(
	id serial primary key,
	name varchar(250),
	text text,
	link text UNIQUE,
	created timestamp
);