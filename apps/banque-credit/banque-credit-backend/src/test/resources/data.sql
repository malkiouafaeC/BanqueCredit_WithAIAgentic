-- Seed accounts for tests (memory H2, same credentials as prod seed)
MERGE INTO app_user (username, password, role, actif) KEY(username) VALUES
  ('conseiller1', '$2a$10$077QwLzhEqxvuOt9QHu5xuFfkxSIaCogv2uAQKjS7tbIM2Y/.THmq', 'CONSEILLER', TRUE),
  ('responsable1', '$2a$10$yOuIzl1LxE5BnNUP3J5YFOZzsaovR1FqJC1G9il7bxFcZMTs4MnXK', 'RESPONSABLE_CREDIT', TRUE);

