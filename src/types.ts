export interface TeamMember {
  id: string;
  name: string;
  email?: string;
  color?: string; // for calendar display
}

export interface Chore {
  id: string;
  title: string;
  description?: string;
  start: Date;
  end: Date;
  assignedTo: string[]; // team member IDs
  isRecurring: boolean;
  recurrenceRule?: RecurrenceRule;
  createdAt: Date;
  updatedAt: Date;
}

export interface RecurrenceRule {
  frequency: 'daily' | 'weekly' | 'monthly' | 'yearly';
  interval: number; // every N days/weeks/months/years
  daysOfWeek?: number[]; // 0-6 for weekly (0 = Sunday)
  dayOfMonth?: number; // for monthly
  month?: number; // for yearly
  endDate?: Date; // optional end date for recurrence
  occurrences?: number; // max number of occurrences
}

export type CalendarView = 'month' | 'week' | 'day' | 'agenda';