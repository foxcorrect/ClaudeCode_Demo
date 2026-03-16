import { addDays, addWeeks, addMonths, addYears, isWithinInterval, startOfDay, endOfDay } from 'date-fns';
import type { Chore, RecurrenceRule } from '../types';

export function generateRecurringEvents(
  chore: Chore,
  startRange: Date,
  endRange: Date
): Date[] {
  if (!chore.isRecurring || !chore.recurrenceRule) {
    return [chore.start];
  }

  const rule = chore.recurrenceRule;
  const dates: Date[] = [];
  let currentDate = new Date(chore.start);

  // Generate occurrences within the range
  while (currentDate <= endRange) {
    if (currentDate >= startRange) {
      dates.push(new Date(currentDate));
    }

    // Stop if we have reached max occurrences
    if (rule.occurrences && dates.length >= rule.occurrences) {
      break;
    }

    // Stop if we have reached end date
    if (rule.endDate && currentDate >= rule.endDate) {
      break;
    }

    // Calculate next occurrence based on frequency
    switch (rule.frequency) {
      case 'daily':
        currentDate = addDays(currentDate, rule.interval);
        break;
      case 'weekly':
        currentDate = addWeeks(currentDate, rule.interval);
        // Adjust to specific days of week if provided
        if (rule.daysOfWeek && rule.daysOfWeek.length > 0) {
          // For weekly with specific days, we need to generate each day in the week
          // This is more complex - for now we'll just use the interval
          // TODO: Implement proper day-of-week handling
        }
        break;
      case 'monthly':
        currentDate = addMonths(currentDate, rule.interval);
        // Adjust to specific day of month if provided
        if (rule.dayOfMonth) {
          currentDate.setDate(rule.dayOfMonth);
        }
        break;
      case 'yearly':
        currentDate = addYears(currentDate, rule.interval);
        // Adjust to specific month and day if provided
        if (rule.month !== undefined) {
          currentDate.setMonth(rule.month);
          if (rule.dayOfMonth) {
            currentDate.setDate(rule.dayOfMonth);
          }
        }
        break;
    }
  }

  return dates;
}

export function isValidRecurrenceRule(rule: RecurrenceRule): boolean {
  if (rule.interval < 1) return false;

  switch (rule.frequency) {
    case 'weekly':
      return !rule.daysOfWeek || rule.daysOfWeek.every(day => day >= 0 && day <= 6);
    case 'monthly':
      return !rule.dayOfMonth || (rule.dayOfMonth >= 1 && rule.dayOfMonth <= 31);
    case 'yearly':
      if (rule.month !== undefined && (rule.month < 0 || rule.month > 11)) return false;
      if (rule.dayOfMonth && (rule.dayOfMonth < 1 || rule.dayOfMonth > 31)) return false;
      return true;
    default:
      return true;
  }
}

export function getRecurrenceDescription(rule: RecurrenceRule): string {
  const interval = rule.interval > 1 ? `every ${rule.interval} ` : 'every ';

  switch (rule.frequency) {
    case 'daily':
      return `Repeats ${interval}day${rule.interval > 1 ? 's' : ''}`;
    case 'weekly':
      if (rule.daysOfWeek && rule.daysOfWeek.length > 0) {
        const days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
        const dayNames = rule.daysOfWeek.map(day => days[day]).join(', ');
        return `Repeats weekly on ${dayNames}`;
      }
      return `Repeats ${interval}week${rule.interval > 1 ? 's' : ''}`;
    case 'monthly':
      if (rule.dayOfMonth) {
        const suffix = rule.dayOfMonth === 1 ? 'st' : rule.dayOfMonth === 2 ? 'nd' : rule.dayOfMonth === 3 ? 'rd' : 'th';
        return `Repeats monthly on the ${rule.dayOfMonth}${suffix}`;
      }
      return `Repeats ${interval}month${rule.interval > 1 ? 's' : ''}`;
    case 'yearly':
      if (rule.month !== undefined) {
        const months = [
          'January', 'February', 'March', 'April', 'May', 'June',
          'July', 'August', 'September', 'October', 'November', 'December'
        ];
        if (rule.dayOfMonth) {
          const suffix = rule.dayOfMonth === 1 ? 'st' : rule.dayOfMonth === 2 ? 'nd' : rule.dayOfMonth === 3 ? 'rd' : 'th';
          return `Repeats yearly on ${months[rule.month]} ${rule.dayOfMonth}${suffix}`;
        }
        return `Repeats yearly in ${months[rule.month]}`;
      }
      return `Repeats ${interval}year${rule.interval > 1 ? 's' : ''}`;
    default:
      return 'Recurring event';
  }
}