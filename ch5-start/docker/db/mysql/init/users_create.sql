create table users (
	id varchar(20) primary key,
	name varchar(20) not null,
	password varchar(20) not null,
	level int not null,
	login int not null,
	recommend int not null
);
