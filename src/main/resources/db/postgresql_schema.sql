-- Create database
CREATE DATABASE shavigh_db;

-- Connect to the database
\c shavigh_db

-- Bible (ՀԻՆ ԿՏԱԿԱՐԱՆ, ՆՈՐ ԿՏԱԿԱՐԱՆ) Tables

-- bible_translations
CREATE TABLE bible_translations (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- bibles
CREATE TABLE bibles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- bible_books
CREATE TABLE bible_books (
    id SERIAL PRIMARY KEY,
    translation_id INT NOT NULL REFERENCES bible_translations(id),
    bible_id INT NOT NULL REFERENCES bibles(id),
    serial_number INT NOT NULL,
    title VARCHAR(255) NOT NULL
);

-- bible_book_chapters
CREATE TABLE bible_book_chapters (
    id SERIAL PRIMARY KEY,
    bible_book_id INT NOT NULL REFERENCES bible_books(id),
    title VARCHAR(50) NOT NULL,
    content TEXT,
    oldUniqueName TEXT,
    url TEXT
);

-- Insert initial bible translations
INSERT INTO bible_translations (name) VALUES
    ('Էջմիածին'),
    ('Արարատ'),
    ('Գրաբար'),
    ('ru');

insert into bible(name) values ('ՀԻՆ ԿՏԱԿԱՐԱՆ');