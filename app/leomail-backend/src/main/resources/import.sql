INSERT INTO account(username, departement, position) VALUES ('IT200285', 'ITM', 'Student');
INSERT INTO account(username, departement, position) VALUES ('IT200274', 'ITM', 'Student');

INSERT INTO templategreeting(id, content, templateString)
VALUES (1, 'Sehr geehrte/r ... ,', '{#if personalized}
    {#if sex == "M"}
        Sehr geehrter Herr {prefixTitle} {lastname} {suffixTitle},
    {/if}
    {#if sex == "W"}
        Sehr geehrte Frau {prefixTitle} {lastname} {suffixTitle},
    {/if}
    {#if sex != "M" && sex != "W"}
        Sehr geehrte {prefixTitle} {lastname} {suffixTitle},
    {/if}
{#else}
    Sehr geehrte Damen und Herren,
{/if}
');

INSERT INTO templategreeting(id, content, templateString)
VALUES (2, 'Liebe/r ... ,', '{#if personalized}
    {#if sex == "M"}
        Lieber {firstname},
    {/if}
    {#if sex == "W"}
        Liebe {firstname},
    {/if}
    {#if sex != "M" && sex != "W"}
        Liebe/r {firstname},
    {/if}
{#else}
    Liebe Damen und Herren,
{/if}
');

insert into contact(id, firstname, lastname, mailaddress) VALUES
                                                              (1, 'Tommy', 'Neumaier', 't.neumaier@students.htl-leonding.ac.at'),
                                                              (2, 'Lana', 'Sekerija', 'l.sekerija@students.htl-leonding.ac.at');

insert into contact_attributes(id, key, val) VALUES
                                                 (1, 'sex', 'M'),
                                                 (2, 'sex', 'W'),
                                                 (1, 'prefixTitle', 'Dr.'),
                                                 (2, 'prefixTitle', 'Ing.'),
                                                 (1, 'suffixTitle', 'PhD'),
                                                 (2, 'suffixTitle', '');

INSERT INTO template(id, name, headline, content, createdBy_username, greeting_id, created) VALUES
                                                                                          (1, 'Kontaktdateninformation (TEST)', 'Ihre Kontaktdaten', '<p>hier eine Verständigung über Ihre Stammdaten, welche in unseren Unternehmen hinterlegt sind:</p><p><br></p><p><u>Vollständiger Name</u></p><p>{firstname} {lastname}</p><p><br></p><p><u>E-Mail-Adresse</u></p><p>{mailAddress}</p><p><br></p><p>Sollten sich Änderungen ergeben haben, bitte melden Sie sich bei uns.</p><p><br></p><p>Mit freundlichen Grüßen</p><p>Max Mustermann</p>', 'IT200274', 1, '2024-07-16 14:26:22.530728'),
                                                                                          (3, 'Begrüßung', 'Herzlich Willkommen!', '<p>Willkommen {firstname} {lastname} bei unserem Unternehmen.</p><p><br></p><p>Ihre E-Mail-Adresse ist {mailAddress}</p><p><br></p><p>Mit freundlichen Grüßen</p><p>Max Mustermann</p>', 'IT200285', 2, '2024-07-16 14:26:22.530728');