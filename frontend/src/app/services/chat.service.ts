import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PromptRequest, PromptResponse } from '../models/chat.model';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private readonly apiUrl = `${environment.backendUrl}/api/llm`;

  constructor(private http: HttpClient) {}

  sendMessage(prompt: string, userId: string, conversationId?: string | null): Observable<PromptResponse> {
    const request: PromptRequest = {
      prompt,
      userId,
      ...(conversationId && { conversationId })
    };

    return this.http.post<PromptResponse>(`${this.apiUrl}/prompt`, request);
  }
}