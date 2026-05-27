import { createClient } from '@supabase/supabase-js';

const supabaseUrl = process.env.REACT_APP_SUPABASE_URL;
const supabaseAnonKey = process.env.REACT_APP_SUPABASE_ANON_KEY;
console.log('Supabase URL:', supabaseUrl);
export const supabase = createClient(supabaseUrl, supabaseAnonKey);

export const getSessionToken = async () => {
  const { data } = await supabase.auth.getSession();
  return data?.session?.access_token || null;
};