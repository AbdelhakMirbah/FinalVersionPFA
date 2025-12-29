import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './components/sidebar/sidebar.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, SidebarComponent],
  template: `
    <div class="flex min-h-screen bg-gray-900 text-gray-100 font-sans">
       <!-- Sidebar -->
       <app-sidebar></app-sidebar>
       
       <!-- Main Content -->
       <main class="flex-1 ml-64 p-8">
           <router-outlet></router-outlet>
       </main>
    </div>
  `
})
export class AppComponent {
  title = 'Real-Time Fraud Monitor';
}
