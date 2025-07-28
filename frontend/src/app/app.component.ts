import { Component, signal, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
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
export class AppComponent implements AfterViewChecked {
  // Using signals for zoneless Angular
  messages = signal<Message[]>([]);
  currentMessage = signal('');
  conversationId = signal<string | null>(null);
  isLoading = signal(false);
  
  @ViewChild('messagesContainer') private messagesContainer!: ElementRef;
  private shouldScrollToBottom = false;
  
  // TODO
  private readonly userId = 'default';

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
    
    // Only scroll to bottom when user sends a message
    this.shouldScrollToBottom = true;

    // Call the backend API
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
          
          // Remove this line - don't scroll when model responds
          // this.shouldScrollToBottom = true;
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
          
          // Remove this line - don't scroll on error either
          // this.shouldScrollToBottom = true;
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