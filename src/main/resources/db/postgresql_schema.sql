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
    title VARCHAR(255) NOT NULL,
    oldUniqueName TEXT,
    url TEXT
);

-- bible_book_chapters
CREATE TABLE bible_book_chapters (
    id SERIAL PRIMARY KEY,
    bible_book_id INT NOT NULL REFERENCES bible_books(id),
    title VARCHAR(50) NOT NULL,
    content TEXT,
    old_unique_name TEXT,
    url TEXT
);

-- Insert initial bible translations
INSERT INTO bible_translations (id, name) VALUES
    (1, 'Էջմիածին'),
    (2, 'Արարատ'),
    (3, 'Գրաբար'),
    (4, 'ru');

insert into bibles(id, name) values (1,'ՀԻՆ ԿՏԱԿԱՐԱՆ');
insert into bibles(id, name) values (2, 'ՆՈՐ ԿՏԱԿԱՐԱՆ');