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
    url TEXT,
    url_armenian TEXT,
    link_to_default_content TEXT,
    next_link TEXT,
    prev_link TEXT,
    unexpected_link BOOLEAN
);

-- bible_book_chapters_pages
CREATE TABLE bible_book_chapter_pages (
    id SERIAL PRIMARY KEY,
    bible_book_chapter_id INT NOT NULL REFERENCES bible_book_chapters(id),
    title VARCHAR(300) NOT NULL,
    content TEXT,
    old_unique_name TEXT,
    url TEXT,
    url_armenian TEXT,
    next_link TEXT,
    prev_link TEXT,
    has_nested_links BOOLEAN DEFAULT FALSE
);

-- Insert initial bible translations
INSERT INTO bible_translations (id, name) VALUES
    (1, 'Էջմիածին'),
    (2, 'Արարատ'),
    (3, 'Գրաբար'),
    (4, 'ru');

insert into bibles(id, name) values (1,'ՀԻՆ ԿՏԱԿԱՐԱՆ');
insert into bibles(id, name) values (2, 'ՆՈՐ ԿՏԱԿԱՐԱՆ');

ALTER TABLE bibles
ADD COLUMN unique_name VARCHAR(255);

ALTER TABLE bibles
ADD CONSTRAINT unique_name_unique UNIQUE (unique_name);

UPDATE bibles
SET unique_name = 'oldTestament'
WHERE id = 1;

UPDATE bibles
SET unique_name = 'newTestament'
WHERE id = 2;

ALTER TABLE bible_book_chapter_pages
ALTER COLUMN title TYPE VARCHAR(500);

ALTER TABLE bible_book_chapter_pages
ALTER COLUMN url TYPE VARCHAR(500);

ALTER TABLE bible_book_chapter_pages
ALTER COLUMN url_armenian TYPE VARCHAR(500);

ALTER TABLE bible_book_chapter_pages
ALTER COLUMN title TYPE TEXT;

ALTER TABLE bible_book_chapter_pages
ALTER COLUMN url TYPE TEXT;

ALTER TABLE bible_book_chapter_pages
ALTER COLUMN url_armenian TYPE TEXT;

-- for bible_book_chapters translation_id = 2
-- Update `url`
UPDATE bible_book_chapters bc
SET url = REPLACE(bc.url, 'bible/echmiadzin/', 'bible/ararat/')
FROM bible_books bb
WHERE bc.bible_book_id = bb.id
  AND bb.translation_id = 2
  AND bc.url LIKE 'bible/echmiadzin/%';

-- Update `prev_link`
UPDATE bible_book_chapters bc
SET prev_link = REPLACE(bc.prev_link, 'bible/echmiadzin/', 'bible/ararat/')
FROM bible_books bb
WHERE bc.bible_book_id = bb.id
  AND bb.translation_id = 2
  AND bc.prev_link LIKE 'bible/echmiadzin/%';

-- Update `next_link`
UPDATE bible_book_chapters bc
SET next_link = REPLACE(bc.next_link, 'bible/echmiadzin/', 'bible/ararat/')
FROM bible_books bb
WHERE bc.bible_book_id = bb.id
  AND bb.translation_id = 2
  AND bc.next_link LIKE 'bible/echmiadzin/%';


-- for bible_book_chapters translation_id = 3
-- url
UPDATE bible_book_chapters bc
SET url = REPLACE(bc.url, 'bible/echmiadzin/', 'bible/grabar/')
FROM bible_books bb
WHERE bc.bible_book_id = bb.id
  AND bb.translation_id = 3
  AND bc.url LIKE 'bible/echmiadzin/%';

-- prev_link
UPDATE bible_book_chapters bc
SET prev_link = REPLACE(bc.prev_link, 'bible/echmiadzin/', 'bible/grabar/')
FROM bible_books bb
WHERE bc.bible_book_id = bb.id
  AND bb.translation_id = 3
  AND bc.prev_link LIKE 'bible/echmiadzin/%';

-- next_link
UPDATE bible_book_chapters bc
SET next_link = REPLACE(bc.next_link, 'bible/echmiadzin/', 'bible/grabar/')
FROM bible_books bb
WHERE bc.bible_book_id = bb.id
  AND bb.translation_id = 3
  AND bc.next_link LIKE 'bible/echmiadzin/%';

-- for bible_book_chapters translation_id = 4
-- url
UPDATE bible_book_chapters bc
SET url = REPLACE(bc.url, 'bible/echmiadzin/', 'bible/russian/')
FROM bible_books bb
WHERE bc.bible_book_id = bb.id
  AND bb.translation_id = 4
  AND bc.url LIKE 'bible/echmiadzin/%';

-- prev_link
UPDATE bible_book_chapters bc
SET prev_link = REPLACE(bc.prev_link, 'bible/echmiadzin/', 'bible/russian/')
FROM bible_books bb
WHERE bc.bible_book_id = bb.id
  AND bb.translation_id = 4
  AND bc.prev_link LIKE 'bible/echmiadzin/%';

-- next_link
UPDATE bible_book_chapters bc
SET next_link = REPLACE(bc.next_link, 'bible/echmiadzin/', 'bible/russian/')
FROM bible_books bb
WHERE bc.bible_book_id = bb.id
  AND bb.translation_id = 4
  AND bc.next_link LIKE 'bible/echmiadzin/%';

-- Saints Behavior
CREATE TABLE saints_behavior (
    id SERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    url TEXT,
    status VARCHAR(20) default 'publish'
);

-- Saints Behavior Section
CREATE TABLE saints_behavior_section (
    id SERIAL PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    content TEXT,
    url TEXT,
    status VARCHAR(20) default 'publish',
    saints_behavior_id INT NOT NULL references saints_behavior(id),
    origin_id BIGINT
);

-- Saints Behavior Section Pages
CREATE TABLE saints_behavior_section_page (
    id SERIAL PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT,
    url TEXT,
    status VARCHAR(20) default 'publish',
    saints_behavior_section_id INT NOT NULL references saints_behavior_section(id),
    origin_id BIGINT
);

CREATE TABLE static_pages (
    id SERIAL PRIMARY KEY,
    unique_name VARCHAR(100),
    status VARCHAR(20) default 'publish',
    content TEXT
);

CREATE TABLE articles (
    id SERIAL PRIMARY KEY,
    title text not NULL,
    status VARCHAR(20) default 'publish',
    content TEXT,
    description TEXT,
    url TEXT not NULL
);

ALTER TABLE bible_book_chapters
ADD COLUMN attached boolean NOT NULL DEFAULT TRUE;

ALTER TABLE bible_book_chapter_pages
ADD COLUMN attached boolean NOT NULL DEFAULT TRUE;

ALTER TABLE saints_behavior_section_page
ADD COLUMN attached boolean NOT NULL DEFAULT TRUE;





