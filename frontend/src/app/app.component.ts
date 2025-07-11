import { Component, signal, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from './services/chat.service';
import { Message } from './models/message.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="chat-container">
      <header class="chat-header">
        <h1>ChatGPT Clone</h1>
        <button 
          class="new-conversation-btn" 
          (click)="startNewConversation()"
          [disabled]="isLoading()">
          New Conversation
        </button>
      </header>

      <div class="messages-container" #messagesContainer>
        @for (message of messages(); track $index) {
          <div 
            class="message"
            [class.user-message]="message.role === 'user'"
            [class.model-message]="message.role === 'model'">
            <div class="message-content">
              <div class="message-role">
                {{ message.role === 'user' ? 'You' : 'Assistant' }}
              </div>
              <div class="message-text">{{ message.text }}</div>
            </div>
          </div>
        }
        
        @if (isLoading()) {
          <div class="message model-message">
            <div class="message-content">
              <div class="message-role">Assistant</div>
              <div class="message-text typing">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        }

        @if (messages().length === 0 && !isLoading()) {
          <div class="empty-state">
            <h2>Welcome to ChatGPT Clone</h2>
            <p>Start a conversation by typing a message below.</p>
          </div>
        }
      </div>

      <div class="input-container">
        <form (ngSubmit)="sendMessage()" class="message-form">
          <div class="input-wrapper">
            <textarea
              [(ngModel)]="currentMessage"
              name="message"
              placeholder="Type your message here..."
              class="message-input"
              [disabled]="isLoading()"
              (keydown.enter)="onEnterKey($event)"
              rows="1"></textarea>
            <button 
              type="submit" 
              class="send-btn"
              [disabled]="!currentMessage().trim() || isLoading()">
              Send
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .chat-container {
      display: flex;
      flex-direction: column;
      height: 100vh;
      max-width: 800px;
      margin: 0 auto;
      background: #fff;
    }

    .chat-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem 2rem;
      border-bottom: 1px solid #e5e5e5;
      background: #f8f9fa;
    }

    .chat-header h1 {
      margin: 0;
      font-size: 1.5rem;
      color: #333;
    }

    .new-conversation-btn {
      padding: 0.5rem 1rem;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 6px;
      cursor: pointer;
      font-size: 0.9rem;
      transition: background-color 0.2s;
    }

    .new-conversation-btn:hover:not(:disabled) {
      background: #0056b3;
    }

    .new-conversation-btn:disabled {
      background: #6c757d;
      cursor: not-allowed;
    }

    .messages-container {
      flex: 1;
      overflow-y: auto;
      padding: 1rem;
      display: flex;
      flex-direction: column;
      gap: 1rem;
    }

    .empty-state {
      text-align: center;
      margin: auto;
      color: #666;
    }

    .empty-state h2 {
      color: #333;
      margin-bottom: 0.5rem;
    }

    .input-container {
      padding: 1rem;
      border-top: 1px solid #e5e5e5;
      background: #f8f9fa;
    }

    .message-form {
      width: 100%;
    }

    .input-wrapper {
      display: flex;
      gap: 0.5rem;
      align-items: flex-end;
    }

    .message-input {
      flex: 1;
      padding: 0.75rem;
      border: 1px solid #ddd;
      border-radius: 8px;
      resize: none;
      min-height: 20px;
      max-height: 120px;
      font-family: inherit;
      font-size: 1rem;
      line-height: 1.4;
    }

    .message-input:focus {
      outline: none;
      border-color: #007bff;
      box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.25);
    }

    .send-btn {
      padding: 0.75rem 1rem;
      background: #007bff;
      color: white;
      border: none;
      border-radius: 8px;
      cursor: pointer;
      transition: background-color 0.2s;
    }

    .send-btn:hover:not(:disabled) {
      background: #0056b3;
    }

    .message {
      display: flex;
      max-width: 70%;
    }

    .user-message {
      align-self: flex-end;
    }

    .model-message {
      align-self: flex-start;
    }

    .message-content {
      padding: 0.75rem 1rem;
      border-radius: 12px;
      background: #f1f3f4;
      word-wrap: break-word;
    }

    .user-message .message-content {
      background: #007bff;
      color: white;
    }

    .message-role {
      font-size: 0.75rem;
      font-weight: 600;
      margin-bottom: 0.25rem;
      opacity: 0.8;
    }

    .message-text {
      line-height: 1.4;
      white-space: pre-wrap;
    }

    .typing {
      display: flex;
      align-items: center;
      gap: 2px;
    }

    .typing span {
      height: 8px;
      width: 8px;
      border-radius: 50%;
      background-color: #666;
      animation: typing 1.4s infinite ease-in-out;
    }

    .typing span:nth-child(1) { animation-delay: -0.32s; }
    .typing span:nth-child(2) { animation-delay: -0.16s; }

    @keyframes typing {
      0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
      40% { transform: scale(1); opacity: 1; }
    }

    /* Responsive design */
    @media (max-width: 768px) {
      .chat-header {
        padding: 1rem;
      }
      
      .chat-header h1 {
        font-size: 1.25rem;
      }
      
      .message {
        max-width: 85%;
      }
      
      .messages-container {
        padding: 0.5rem;
      }

      .input-container {
        padding: 0.5rem;
      }
    }
  `]
})
export class AppComponent implements AfterViewChecked {
  // Using signals for zoneless Angular
  messages = signal<Message[]>([]);
  currentMessage = signal('');
  conversationId = signal<string | null>(null);
  isLoading = signal(false);
  
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  private shouldScrollToBottom = false;
  
  private readonly userId = 'test_user';

  constructor(private chatService: ChatService) {}

  ngAfterViewChecked() {
    if (this.shouldScrollToBottom) {
      this.scrollToBottom();
      this.shouldScrollToBottom = false;
    }
  }

  private scrollToBottom(): void {
    try {
      this.messagesContainer.nativeElement.scrollTop = this.messagesContainer.nativeElement.scrollHeight;
    } catch(err) {
      console.error('Error scrolling to bottom:', err);
    }
  }

  sendMessage() {
    const messageText = this.currentMessage().trim();
    if (!messageText || this.isLoading()) {
      return;
    }

    // Add user message to the conversation
    const userMessage: Message = {
      text: messageText,
      role: 'user'
    };
    
    this.messages.update(messages => [...messages, userMessage]);
    this.currentMessage.set('');
    this.isLoading.set(true);
    this.shouldScrollToBottom = true;

    // Call the backend API
    this.chatService.sendMessage(messageText, this.userId, this.conversationId())
      .subscribe({
        next: (response) => {
          // Add assistant response to the conversation
          const assistantMessage: Message = {
            text: response.text,
            role: 'model'
          };
          
          this.messages.update(messages => [...messages, assistantMessage]);
          this.conversationId.set(response.conversationId);
          this.isLoading.set(false);
          this.shouldScrollToBottom = true;
        },
        error: (error) => {
          console.error('Error sending message:', error);
          
          // Add error message to the conversation
          const errorMessage: Message = {
            text: 'Sorry, there was an error processing your message. Please try again.',
            role: 'model'
          };
          
          this.messages.update(messages => [...messages, errorMessage]);
          this.isLoading.set(false);
          this.shouldScrollToBottom = true;
        }
      });
  }

  startNewConversation() {
    this.messages.set([]);
    this.conversationId.set(null);
    this.currentMessage.set('');
  }

  onEnterKey(event: Event) {
    const keyboardEvent = event as KeyboardEvent;
    if (keyboardEvent.key === 'Enter' && !keyboardEvent.shiftKey) {
      keyboardEvent.preventDefault();
      this.sendMessage();
    }
  }
}