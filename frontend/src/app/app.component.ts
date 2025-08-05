import { Component, signal, ElementRef, ViewChild, effect } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MarkdownModule } from 'ngx-markdown';
import { ChatService } from './services/chat.service';
import { Message } from './models/message.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownModule],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})

export class AppComponent {
  // Using signals for zoneless Angular
  messages = signal<Message[]>([]);
  currentMessage = signal('');
  conversationId = signal<string | null>(null);
  isLoading = signal(false);
  
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  
  // TODO
  private readonly userId = 'default';

  constructor(private chatService: ChatService) {
    effect(() => {
      const messages = this.messages();
      if (messages.length > 0) {
        const lastMessage = messages[messages.length - 1];
        
        // Only scroll when the last message is from the user
        if (lastMessage.role === 'user') {
          setTimeout(() => this.scrollToBottom(), 0);
        }
      }
    });
  }

  private scrollToBottom(): void {
    try {
      const container = this.messagesContainer?.nativeElement;
      if (container) {
        container.scrollTop = container.scrollHeight;
      }
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

    // Call the LLM and update the current conversation
    this.chatService.sendMessage(messageText, this.userId, this.conversationId())
      .subscribe({
        next: (response) => {
          // Add model response to the conversation
          const modelMessage: Message = {
            text: response.text,
            role: 'model'
          };
          
          this.messages.update(messages => [...messages, modelMessage]);
          this.conversationId.set(response.conversationId);
          this.isLoading.set(false);
        },
        error: (error) => {
          console.error('Error sending message:', error);
          const errorMessage: Message = {
            text: 'Sorry, there was an error processing your message. Please try again.',
            role: 'model'
          };
          
          this.messages.update(messages => [...messages, errorMessage]);
          this.isLoading.set(false);
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