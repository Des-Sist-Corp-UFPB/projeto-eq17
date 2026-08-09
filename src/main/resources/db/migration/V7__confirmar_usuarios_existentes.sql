-- Ativa o e-mail de todos os usuários existentes no banco de dados para evitar bloqueio de login (como o administrador)
UPDATE usuario SET email_confirmado = TRUE WHERE email_confirmado = FALSE;
