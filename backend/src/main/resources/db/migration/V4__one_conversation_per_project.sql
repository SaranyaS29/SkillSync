ALTER TABLE conversations
    ADD CONSTRAINT uk_conversation_project UNIQUE (project_id);
