# 此项目为claude接入deepseek自动生成的测试项目，启用了deepseek-reasoner模型

**生成指令为：**
make a chore app to manage chroes at the office. It should :

- 1.show a calendar view likeOutlook to see all our current chroes;
- 2.Allow dding/removing chroes;
- 3.Be able to make chroes recurring on a schecule;
- 4.Assign chores to team member;
- 5.add/remove team member

# Office Chores Manager

A React-based application for managing office chores with calendar view, team management, and recurring schedules.

## Features

1. **Calendar View** - Outlook-like calendar to view all chores
2. **Chore Management** - Add, edit, and remove chores
3. **Recurring Chores** - Schedule chores to repeat daily, weekly, monthly, or yearly
4. **Team Management** - Add/remove team members and assign chores
5. **Responsive Design** - Works on desktop and mobile

## Getting Started

### Prerequisites

- Node.js (v18 or higher)
- npm or yarn

### Installation

1. Clone the repository
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the development server:
   ```bash
   npm run dev
   ```
4. Open your browser and navigate to `http://localhost:5173`

## Usage

### Calendar View

- Switch between month, week, and day views
- Click on any chore to view details
- Drag and drop to reschedule chores (future enhancement)

### Adding Chores

1. Navigate to "Manage Chores" tab
2. Click "Add New Chore"
3. Fill in chore details:
   - Title and description
   - Start and end time
   - Assign team members
   - Set recurrence if needed

### Managing Team

1. Navigate to "Manage Team" tab
2. Add team members with names, emails, and colors
3. View each member's assigned chores
4. Remove members (chores will become unassigned)

### Recurring Chores

- Set chores to repeat at regular intervals
- Choose frequency: daily, weekly, monthly, yearly
- Configure specific days for weekly repeats
- Set end date or number of occurrences

## Technology Stack

- **React** with TypeScript
- **Vite** for build tooling
- **Material-UI (MUI)** for UI components
- **react-big-calendar** for calendar view
- **date-fns** for date manipulation
- **MUI X Date Pickers** for date/time selection

## Project Structure

```
src/
├── components/
│   ├── CalendarView.tsx    # Main calendar component
│   ├── ChoreManager.tsx    # Chore CRUD operations
│   └── TeamManager.tsx     # Team member management
├── utils/
│   └── recurrenceUtils.ts  # Recurrence logic
├── types.ts               # TypeScript interfaces
├── App.tsx               # Main app component
└── main.tsx             # Entry point
```

## Future Enhancements

- User authentication
- Drag-and-drop chore rescheduling
- Email notifications
- Chore completion tracking
- Analytics and reports
- Export to CSV/PDF
- Dark mode

## License

This project is open source and available under the MIT License.

## Acknowledgments

- Built with [Vite](https://vite.dev/)
- UI components from [Material-UI](https://mui.com/)
- Calendar from [react-big-calendar](https://github.com/jquense/react-big-calendar)
