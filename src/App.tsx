import { useState } from 'react';
import { Container, AppBar, Toolbar, Typography, Box, Tabs, Tab, CssBaseline } from '@mui/material';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CalendarView from './components/CalendarView';
import ChoreManager from './components/ChoreManager';
import TeamManager from './components/TeamManager';
import type { Chore, TeamMember } from './types';
import './App.css';

// Mock initial data
const initialTeamMembers: TeamMember[] = [
  { id: '1', name: 'Alice Johnson', email: 'alice@example.com', color: '#ff6b6b' },
  { id: '2', name: 'Bob Smith', email: 'bob@example.com', color: '#4ecdc4' },
  { id: '3', name: 'Charlie Brown', email: 'charlie@example.com', color: '#ffd166' },
  { id: '4', name: 'Diana Prince', email: 'diana@example.com', color: '#06d6a0' },
];

const initialChores: Chore[] = [
  {
    id: '1',
    title: 'Clean kitchen',
    description: 'Wipe counters, clean sink, empty trash',
    start: new Date(new Date().setHours(9, 0, 0, 0)),
    end: new Date(new Date().setHours(10, 0, 0, 0)),
    assignedTo: ['1', '2'],
    isRecurring: true,
    recurrenceRule: {
      frequency: 'weekly',
      interval: 1,
      daysOfWeek: [1, 4], // Monday and Thursday
    },
    createdAt: new Date(),
    updatedAt: new Date(),
  },
  {
    id: '2',
    title: 'Restock supplies',
    description: 'Check and restock office supplies',
    start: new Date(new Date().setDate(new Date().getDate() + 1)),
    end: new Date(new Date().setDate(new Date().getDate() + 1)),
    assignedTo: ['3'],
    isRecurring: false,
    createdAt: new Date(),
    updatedAt: new Date(),
  },
];

const theme = createTheme({
  palette: {
    primary: {
      main: '#2c3e50',
    },
    secondary: {
      main: '#3498db',
    },
    background: {
      default: '#f5f7fa',
    },
  },
  typography: {
    fontFamily: '"Segoe UI", "Roboto", "Helvetica", "Arial", sans-serif',
  },
});

function App() {
  const [tabIndex, setTabIndex] = useState(0);
  const [teamMembers, setTeamMembers] = useState<TeamMember[]>(initialTeamMembers);
  const [chores, setChores] = useState<Chore[]>(initialChores);

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabIndex(newValue);
  };

  const addChore = (chore: Chore) => {
    setChores([...chores, chore]);
  };

  const updateChore = (id: string, updatedChore: Partial<Chore>) => {
    setChores(chores.map(chore =>
      chore.id === id ? { ...chore, ...updatedChore, updatedAt: new Date() } : chore
    ));
  };

  const deleteChore = (id: string) => {
    setChores(chores.filter(chore => chore.id !== id));
  };

  const addTeamMember = (member: TeamMember) => {
    setTeamMembers([...teamMembers, member]);
  };

  const deleteTeamMember = (id: string) => {
    // Reassign chores assigned to this member to unassigned
    const updatedChores = chores.map(chore => ({
      ...chore,
      assignedTo: chore.assignedTo.filter(memberId => memberId !== id),
    }));
    setChores(updatedChores);
    setTeamMembers(teamMembers.filter(member => member.id !== id));
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <AppBar position="static">
          <Toolbar>
            <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
              Office Chores Manager
            </Typography>
            <Typography variant="body2">
              {teamMembers.length} team members • {chores.length} active chores
            </Typography>
          </Toolbar>
        </AppBar>

        <Box sx={{ flexGrow: 1, p: 3 }}>
          <Container maxWidth="xl">
            <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
              <Tabs value={tabIndex} onChange={handleTabChange} aria-label="chore app tabs">
                <Tab label="Calendar View" />
                <Tab label="Manage Chores" />
                <Tab label="Manage Team" />
              </Tabs>
            </Box>

            {tabIndex === 0 && (
              <CalendarView
                chores={chores}
                teamMembers={teamMembers}
                onChoreUpdate={updateChore}
                onChoreDelete={deleteChore}
              />
            )}

            {tabIndex === 1 && (
              <ChoreManager
                chores={chores}
                teamMembers={teamMembers}
                onAddChore={addChore}
                onUpdateChore={updateChore}
                onDeleteChore={deleteChore}
              />
            )}

            {tabIndex === 2 && (
              <TeamManager
                teamMembers={teamMembers}
                chores={chores}
                onAddMember={addTeamMember}
                onDeleteMember={deleteTeamMember}
              />
            )}
          </Container>
        </Box>

        <Box sx={{ p: 2, bgcolor: 'grey.100', textAlign: 'center' }}>
          <Typography variant="body2" color="text.secondary">
            Office Chores Manager • Keep your workspace clean and organized
          </Typography>
        </Box>
      </Box>
    </ThemeProvider>
  );
}

export default App;
