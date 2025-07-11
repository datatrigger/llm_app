export interface Message {
  text: string;
  role: 'user' | 'model';
}