INSERT INTO account(username, departement, position) VALUES ('IT200285', 'ITM', 'Student');
INSERT INTO account(username, departement, position) VALUES ('IT200274', 'ITM', 'Student');

INSERT INTO template(id, content, headline, name, createdby_username)
VALUES (1, 'Das ist der Content', 'Headline #1', 'Die beste Vorlage', 'IT200285'),
       (2, 'Das ist der zweite Content', 'Headline #2', 'Die zweitbeste Vorlage', 'IT200274');

INSERT INTO templategreeting(id, content) VALUES (1, 'Hallo, ich bin ein Gruß!'),
(2, 'Hallo, ich bin ein weiterer Gruß!'),
(3, 'Hallo, ich bin ein dritter Gruß!');