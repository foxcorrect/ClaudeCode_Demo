import React, { useState } from 'react';
import { Calendar, momentLocalizer } from 'react-big-calendar';
import type { View } from 'react-big-calendar';
import moment from 'moment';
import 'react-big-calendar/lib/css/react-big-calendar.css';
import {
  Box,
  Paper,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import {
  Edit as EditIcon,
  Delete as DeleteIcon,
  Person as PersonIcon,
  Today as TodayIcon,
} from '@mui/icons-material';
import type { Chore, TeamMember } from '../types';
import { generateRecurringEvents } from '../utils/recurrenceUtils';

const localizer = momentLocalizer(moment);

interface CalendarViewProps {
  chores: Chore[];
  teamMembers: TeamMember[];
  onChoreUpdate: (id: string, updatedChore: Partial<Chore>) => void;
  onChoreDelete: (id: string) => void;
}

interface CalendarEvent {
  id: string;
  title: string;
  start: Date;
  end: Date;
  chore: Chore;
  assignedTo: TeamMember[];
}

const CalendarView: React.FC<CalendarViewProps> = ({
  chores,
  teamMembers,
  onChoreUpdate,
  onChoreDelete,
}) => {
  const [currentView, setCurrentView] = useState<View>('week');
  const [currentDate, setCurrentDate] = useState<Date>(new Date());
  const [selectedEvent, setSelectedEvent] = useState<CalendarEvent | null>(null);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [editChoreData, setEditChoreData] = useState<Partial<Chore> | null>(null);

  // Generate calendar events from chores (including recurring ones)
  const generateEvents = (): CalendarEvent[] => {
    const events: CalendarEvent[] = [];
    const startDate = moment(currentDate).startOf('month').subtract(1, 'month').toDate();
    const endDate = moment(currentDate).endOf('month').add(1, 'month').toDate();

    chores.forEach(chore => {
      if (chore.isRecurring && chore.recurrenceRule) {
        const recurringDates = generateRecurringEvents(
          chore,
          startDate,
          endDate
        );
        recurringDates.forEach(date => {
          const start = new Date(date);
          const end = new Date(start.getTime() + (chore.end.getTime() - chore.start.getTime()));
          events.push({
            id: `${chore.id}-${date.getTime()}`,
            title: chore.title,
            start,
            end,
            chore,
            assignedTo: teamMembers.filter(member => chore.assignedTo.includes(member.id)),
          });
        });
      } else {
        events.push({
          id: chore.id,
          title: chore.title,
          start: chore.start,
          end: chore.end,
          chore,
          assignedTo: teamMembers.filter(member => chore.assignedTo.includes(member.id)),
        });
      }
    });

    return events;
  };

  const events = generateEvents();

  const handleSelectEvent = (event: CalendarEvent) => {
    setSelectedEvent(event);
  };

  const handleCloseDetails = () => {
    setSelectedEvent(null);
  };

  const handleEditClick = () => {
    if (selectedEvent) {
      setEditChoreData(selectedEvent.chore);
      setIsEditDialogOpen(true);
    }
  };

  const handleDeleteClick = () => {
    if (selectedEvent) {
      onChoreDelete(selectedEvent.chore.id);
      setSelectedEvent(null);
    }
  };

  const handleEditSave = () => {
    if (editChoreData && selectedEvent) {
      onChoreUpdate(selectedEvent.chore.id, editChoreData);
      setIsEditDialogOpen(false);
      setSelectedEvent(null);
    }
  };

  const handleEditCancel = () => {
    setIsEditDialogOpen(false);
    setEditChoreData(null);
  };

  const eventStyleGetter = (event: CalendarEvent) => {
    const assignedCount = event.assignedTo.length;
    const color = assignedCount > 0 ? event.assignedTo[0]?.color || '#3498db' : '#95a5a6';

    return {
      style: {
        backgroundColor: color,
        borderRadius: '4px',
        opacity: 0.8,
        color: 'white',
        border: '0',
        display: 'block',
      },
    };
  };

  return (
    <Box sx={{ display: 'flex', gap: 3 }}>
      <Box sx={{ flexGrow: 1 }}>
        <Paper elevation={2} sx={{ p: 2, mb: 2 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h5" gutterBottom>
              Chores Calendar
            </Typography>
            <Box sx={{ display: 'flex', gap: 1 }}>
              <Button
                variant={currentView === 'month' ? 'contained' : 'outlined'}
                onClick={() => setCurrentView('month')}
                size="small"
              >
                Month
              </Button>
              <Button
                variant={currentView === 'week' ? 'contained' : 'outlined'}
                onClick={() => setCurrentView('week')}
                size="small"
              >
                Week
              </Button>
              <Button
                variant={currentView === 'day' ? 'contained' : 'outlined'}
                onClick={() => setCurrentView('day')}
                size="small"
              >
                Day
              </Button>
              <Button
                variant="outlined"
                startIcon={<TodayIcon />}
                onClick={() => setCurrentDate(new Date())}
                size="small"
              >
                Today
              </Button>
            </Box>
          </Box>

          <Box sx={{ height: 600 }}>
            <Calendar
              localizer={localizer}
              events={events}
              startAccessor="start"
              endAccessor="end"
              style={{ height: '100%' }}
              view={currentView}
              onView={setCurrentView}
              date={currentDate}
              onNavigate={setCurrentDate}
              onSelectEvent={handleSelectEvent}
              eventPropGetter={eventStyleGetter}
              components={{
                event: ({ event }: { event: CalendarEvent }) => (
                  <div style={{ padding: '2px 4px', overflow: 'hidden' }}>
                    <div style={{ fontWeight: 'bold', fontSize: '12px' }}>{event.title}</div>
                    <div style={{ fontSize: '10px', opacity: 0.9 }}>
                      {event.assignedTo.map(m => m.name).join(', ')}
                    </div>
                  </div>
                ),
              }}
            />
          </Box>
        </Paper>
      </Box>

      {selectedEvent && (
        <Box sx={{ width: 320 }}>
          <Paper elevation={2} sx={{ p: 2 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6">{selectedEvent.title}</Typography>
              <Box>
                <Tooltip title="Edit">
                  <IconButton onClick={handleEditClick} size="small">
                    <EditIcon />
                  </IconButton>
                </Tooltip>
                <Tooltip title="Delete">
                  <IconButton onClick={handleDeleteClick} size="small" color="error">
                    <DeleteIcon />
                  </IconButton>
                </Tooltip>
              </Box>
            </Box>

            <Typography variant="body2" color="text.secondary" paragraph>
              {selectedEvent.chore.description || 'No description'}
            </Typography>

            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" gutterBottom>
                <strong>Time:</strong>
              </Typography>
              <Typography variant="body2">
                {moment(selectedEvent.start).format('LLL')} - {moment(selectedEvent.end).format('LT')}
              </Typography>
            </Box>

            {selectedEvent.chore.isRecurring && (
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle2" gutterBottom>
                  <strong>Recurrence:</strong>
                </Typography>
                <Chip
                  label={`Repeats ${selectedEvent.chore.recurrenceRule?.frequency}`}
                  size="small"
                  color="primary"
                  variant="outlined"
                />
              </Box>
            )}

            <Box sx={{ mb: 2 }}>
              <Typography variant="subtitle2" gutterBottom>
                <strong>Assigned to:</strong>
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {selectedEvent.assignedTo.length > 0 ? (
                  selectedEvent.assignedTo.map(member => (
                    <Chip
                      key={member.id}
                      icon={<PersonIcon />}
                      label={member.name}
                      size="small"
                      sx={{ backgroundColor: member.color || '#e0e0e0' }}
                    />
                  ))
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    Unassigned
                  </Typography>
                )}
              </Box>
            </Box>

            <Button
              variant="outlined"
              fullWidth
              onClick={handleCloseDetails}
              sx={{ mt: 2 }}
            >
              Close Details
            </Button>
          </Paper>
        </Box>
      )}

      {/* Edit Dialog */}
      <Dialog open={isEditDialogOpen} onClose={handleEditCancel} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Chore</DialogTitle>
        <DialogContent>
          {editChoreData && (
            <Box sx={{ pt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
              <TextField
                label="Title"
                value={editChoreData.title || ''}
                onChange={(e) => setEditChoreData({ ...editChoreData, title: e.target.value })}
                fullWidth
              />
              <TextField
                label="Description"
                value={editChoreData.description || ''}
                onChange={(e) => setEditChoreData({ ...editChoreData, description: e.target.value })}
                multiline
                rows={3}
                fullWidth
              />
              <FormControl fullWidth>
                <InputLabel>Assigned To</InputLabel>
                <Select
                  multiple
                  value={editChoreData.assignedTo || []}
                  onChange={(e) => setEditChoreData({ ...editChoreData, assignedTo: e.target.value as string[] })}
                  label="Assigned To"
                  renderValue={(selected) => (
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {selected.map(memberId => {
                        const member = teamMembers.find(m => m.id === memberId);
                        return member ? (
                          <Chip key={memberId} label={member.name} size="small" />
                        ) : null;
                      })}
                    </Box>
                  )}
                >
                  {teamMembers.map(member => (
                    <MenuItem key={member.id} value={member.id}>
                      {member.name}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleEditCancel}>Cancel</Button>
          <Button onClick={handleEditSave} variant="contained">
            Save Changes
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default CalendarView;