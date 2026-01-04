import { createClient } from '@supabase/supabase-js'

// Remplace ces valeurs par CELLES DE TON PROJET SUPABASE (Les mêmes que dans Android)
const supabaseUrl = 'https://nvwigxaexrxwolezvmsl.supabase.co'
const supabaseKey = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im52d2lneGFleHJ4d29sZXp2bXNsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU3NDY0MzIsImV4cCI6MjA4MTMyMjQzMn0.0H2fW2CN9C-AxWSMNmZy87BKv7WvSuDk2yfM5M61WC4'

export const supabase = createClient(supabaseUrl, supabaseKey)