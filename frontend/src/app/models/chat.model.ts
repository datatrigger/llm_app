export interface PromptRequest {
  prompt: string;
  userId: string;
  conversationId?: string;
}

export interface PromptResponse {
  text: string;
  conversationId: string;
}