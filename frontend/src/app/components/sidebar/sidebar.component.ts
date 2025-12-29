import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  template: `
    <aside class="fixed left-0 top-0 h-screen w-64 bg-gray-900 border-r border-gray-800 flex flex-col z-50">
      <!-- Logo -->
      <div class="p-6 flex items-center gap-3 border-b border-gray-800">
        <div class="w-8 h-8 bg-blue-600 rounded-lg flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
          </svg>
        </div>
        <span class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-blue-400 to-purple-500">
          FraudGuard
        </span>
      </div>

      <!-- Navigation -->
      <nav class="flex-1 p-4 space-y-2 overflow-y-auto">
        
        <div class="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 px-2 mt-4">Menu</div>

        <a routerLink="/dashboard" routerLinkActive="bg-blue-600/10 text-blue-400 border-r-2 border-blue-500" 
           class="flex items-center gap-3 p-3 text-gray-400 rounded-lg hover:bg-gray-800 hover:text-gray-100 transition-all font-medium">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
             <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 10V3L4 14h7v7l9-11h-7z" />
          </svg>
          Live Monitor
        </a>

        <a routerLink="/history" routerLinkActive="bg-blue-600/10 text-blue-400 border-r-2 border-blue-500" 
           class="flex items-center gap-3 p-3 text-gray-400 rounded-lg hover:bg-gray-800 hover:text-gray-100 transition-all font-medium">
          <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          History & Reports
        </a>

      </nav>

      <!-- User Profile (Bottom) -->
      <div class="p-4 border-t border-gray-800">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full bg-gray-700 flex items-center justify-center text-gray-300 font-bold">
            AD
          </div>
          <div>
            <div class="text-sm font-medium text-white">Admin User</div>
            <div class="text-xs text-gray-500">System Administrator</div>
          </div>
        </div>
      </div>
    </aside>
  `
})
export class SidebarComponent { }
