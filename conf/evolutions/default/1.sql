# Initial version
# --- !Ups

create table "tasks" (
  "id" SERIAL not null primary key,
  "title" varchar not null,
  "description" varchar,
  "state" varchar not null
);

# --- !Downs

drop table "tasks" if exists;