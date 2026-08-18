ALTER TABLE IF EXISTS public.periodic_change_rule
ADD COLUMN active BOOLEAN DEFAULT true NOT NULL;
