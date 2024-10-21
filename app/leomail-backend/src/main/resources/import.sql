-- 1. Insert into templategreeting
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
'),
       (2, 'Liebe/r ... ,', '{#if personalized}
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

-- 2. Insert into contact
INSERT INTO contact(id, kcuser, created)
VALUES
    ('1d1d1d1d-1111-1111-1111-111111111111', false, NOW()),
    ('2d2d2d2d-2222-2222-2222-222222222222', false, NOW()),
    ('3d3d3d3d-3333-3333-3333-333333333333', false, NOW()),
    ('3e1d204f-fde9-4f3a-9fc6-b8b0d1a05600', true, NOW()),
    ('b06fe6df-f4b3-4bbc-a119-51ce6eacfd6d', true, NOW()),
    ('3e2fe810-93c7-4276-b810-0a2edff476b4', true, NOW());

-- 3. Insert into naturalcontact
INSERT INTO naturalcontact(id, firstname, lastname, mailaddress, gender)
VALUES
    ('1d1d1d1d-1111-1111-1111-111111111111', 'Lana', 'Sekerija (TEST)', 'sekerija.lana@gmail.com', 'W'),
    ('2d2d2d2d-2222-2222-2222-222222222222', 'Tommy', 'Neumaier (TEST)', 'neumaier.tommy@gmail.com', 'M'),
    ('3d3d3d3d-3333-3333-3333-333333333333', 'Thomas', 'Müller (TEST)', 't.m@esn.com', 'M'),
    ('3e1d204f-fde9-4f3a-9fc6-b8b0d1a05600', 'Tommy', 'Neumaier', 't.neumaier@students.htl-leonding.ac.at', 'M'),
    ('b06fe6df-f4b3-4bbc-a119-51ce6eacfd6d', 'Tommy', 'Neumaier', 'tn@tommyneumaier.at', 'M'),
    ('3e2fe810-93c7-4276-b810-0a2edff476b4', 'Lana', 'Sekerija', 'l.sekerija@students.htl-leonding.ac.at', 'W');

-- 4. If you have any company contacts, insert them into companycontact table similarly.

-- 5. Insert into project
INSERT INTO project(id, name, description, createdon, mailaddress, password, createdby_id)
VALUES
    ('123e4567-e89b-12d3-a456-426614174000', 'Test Project', 'This is a test project', NOW(), 'project@example.com',
     'password', '3e1d204f-fde9-4f3a-9fc6-b8b0d1a05600');

-- 6. Insert into template
INSERT INTO template(id, name, headline, content, createdby_id, greeting_id, created, project_id)
VALUES
    (1, 'Kontaktdateninformation (TEST)', 'Ihre Kontaktdaten', '<p>hier eine Verständigung über Ihre Stammdaten, welche in unseren Unternehmen hinterlegt sind:</p><p><br></p><p><u>Vollständiger Name</u></p><p>{firstname} {lastname}</p><p><br></p><p><u>E-Mail-Adresse</u></p><p>{mailAddress}</p><p><br></p><p>Sollten sich Änderungen ergeben haben, bitte melden Sie sich bei uns.</p><p><br></p><p>Mit freundlichen Grüßen</p><p>Max Mustermann</p>',
     '1d1d1d1d-1111-1111-1111-111111111111', 1, '2024-07-16 14:26:22.530728', '123e4567-e89b-12d3-a456-426614174000'),
    (3, 'Begrüßung', 'Herzlich Willkommen!', '<p>Willkommen {firstname} {lastname} bei unserem Unternehmen.</p><p><br></p><p>Ihre E-Mail-Adresse ist {mailAddress}</p><p><br></p><p>Mit freundlichen Grüßen</p><p>Max Mustermann</p>',
     '2d2d2d2d-2222-2222-2222-222222222222', 2, '2024-07-16 14:26:22.530728', '123e4567-e89b-12d3-a456-426614174000');


-- 7. Insert into project_contact
INSERT INTO project_contact(project_id, contact_id)
VALUES
    ('123e4567-e89b-12d3-a456-426614174000', '3e1d204f-fde9-4f3a-9fc6-b8b0d1a05600'),
    ('123e4567-e89b-12d3-a456-426614174000', '3e2fe810-93c7-4276-b810-0a2edff476b4'),
    ('123e4567-e89b-12d3-a456-426614174000', 'b06fe6df-f4b3-4bbc-a119-51ce6eacfd6d');