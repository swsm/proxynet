CREATE TABLE public."user" (
   id varchar NOT NULL,
   "name" varchar NULL,
   age int4 NULL,
   CONSTRAINT user_pk PRIMARY KEY (id)
);
INSERT INTO public."user"(id, "name", age) VALUES ('1', '1', 1);
INSERT INTO public."user"(id, "name", age) VALUES ('2', '2', 2);
