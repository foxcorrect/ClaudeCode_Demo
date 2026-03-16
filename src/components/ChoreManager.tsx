import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Switch,
  FormControlLabel,
  Grid,
  Card,
  CardContent,
  CardActions,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Tabs,
  Tab,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Schedule as ScheduleIcon,
  Person as PersonIcon,
} from '@mui/icons-material';
import { DatePicker, TimePicker } from '@mui/x-date-pickers';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import type { Chore, TeamMember, RecurrenceRule } from '../types';
import { getRecurrenceDescription, isValidRecurrenceRule } from '../utils/recurrenceUtils';

interface ChoreManagerProps {
  chores: Chore[];
  teamMembers: TeamMember[];
  onAddChore: (chore: Chore) => void;
  onUpdateChore: (id: string, updatedChore: Partial<Chore>) => void;
  onDeleteChore: (id: string) => void;
}

const ChoreManager: React.FC<ChoreManagerProps> = ({
  chores,
  teamMembers,
  onAddChore,
  onUpdateChore,
  onDeleteChore,
}) => {
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [activeTab, setActiveTab] = useState(0);
  const [selectedChore, setSelectedChore] = useState<Chore | null>(null);
  const [newChore, setNewChore] = useState<Partial<Chore>>({
    title: '',
    description: '',
    start: new Date(),
    end: new Date(Date.now() + 60 * 60 * 1000), // 1 hour later
    assignedTo: [],
    isRecurring: false,
    recurrenceRule: {
      frequency: 'weekly',
      interval: 1,
    },
  });

  const handleOpenAddDialog = () => {
    setNewChore({
      title: '',
      description: '',
      start: new Date(),
      end: new Date(Date.now() + 60 * 60 * 1000),
      assignedTo: [],
      isRecurring: false,
      recurrenceRule: {
        frequency: 'weekly',
        interval: 1,
      },
    });
    setIsAddDialogOpen(true);
  };

  const handleCloseAddDialog = () => {
    setIsAddDialogOpen(false);
  };

  const handleOpenEditDialog = (chore: Chore) => {
    setSelectedChore(chore);
    setIsEditDialogOpen(true);
  };

  const handleCloseEditDialog = () => {
    setIsEditDialogOpen(false);
    setSelectedChore(null);
  };

  const handleAddChore = () => {
    const chore: Chore = {
      id: Date.now().toString(),
      title: newChore.title!,
      description: newChore.description,
      start: newChore.start!,
      end: newChore.end!,
      assignedTo: newChore.assignedTo!,
      isRecurring: newChore.isRecurring!,
      recurrenceRule: newChore.isRecurring ? newChore.recurrenceRule : undefined,
      createdAt: new Date(),
      updatedAt: new Date(),
    };
    onAddChore(chore);
    setIsAddDialogOpen(false);
  };

  const handleUpdateChore = () => {
    if (selectedChore) {
      onUpdateChore(selectedChore.id, {
        ...newChore,
        updatedAt: new Date(),
      });
      setIsEditDialogOpen(false);
      setSelectedChore(null);
    }
  };

  const handleDeleteChore = (id: string) => {
    if (window.confirm('Are you sure you want to delete this chore?')) {
      onDeleteChore(id);
    }
  };

  const updateNewChoreField = (field: keyof Chore, value: any) => {
    setNewChore({ ...newChore, [field]: value });
  };

  const updateRecurrenceField = (field: keyof RecurrenceRule, value: any) => {
    setNewChore({
      ...newChore,
      recurrenceRule: {
        ...newChore.recurrenceRule!,
        [field]: value,
      },
    });
  };

  const isChoreValid = () => {
    if (!newChore.title?.trim()) return false;
    if (!newChore.start || !newChore.end) return false;
    if (newChore.end <= newChore.start) return false;
    if (newChore.isRecurring && newChore.recurrenceRule) {
      return isValidRecurrenceRule(newChore.recurrenceRule);
    }
    return true;
  };

  const renderRecurrenceSettings = () => (
    <Box sx={{ mt: 2, p: 2, bgcolor: 'grey.50', borderRadius: 1 }}>
      <Typography variant="subtitle2" gutterBottom>
        Recurrence Settings
      </Typography>
      <Grid container spacing={2}>
        {/* @ts-ignore */}
        <Grid item xs={6} component="div">
          <FormControl fullWidth>
            <InputLabel>Frequency</InputLabel>
            <Select
              value={newChore.recurrenceRule?.frequency || 'weekly'}
              onChange={(e) => updateRecurrenceField('frequency', e.target.value)}
              label="Frequency"
            >
              <MenuItem value="daily">Daily</MenuItem>
              <MenuItem value="weekly">Weekly</MenuItem>
              <MenuItem value="monthly">Monthly</MenuItem>
              <MenuItem value="yearly">Yearly</MenuItem>
            </Select>
          </FormControl>
        </Grid>
        {/* @ts-ignore */}
        <Grid item xs={6} component="div">
          <TextField
            label="Interval"
            type="number"
            value={newChore.recurrenceRule?.interval || 1}
            onChange={(e) => updateRecurrenceField('interval', parseInt(e.target.value) || 1)}
            fullWidth
            InputProps={{ inputProps: { min: 1 } }}
          />
        </Grid>
        {newChore.recurrenceRule?.frequency === 'weekly' && (
          <Grid item xs={12} component="div">
            <FormControl fullWidth>
              <InputLabel>Days of Week</InputLabel>
              <Select
                multiple
                value={newChore.recurrenceRule?.daysOfWeek || []}
                onChange={(e) => updateRecurrenceField('daysOfWeek', e.target.value)}
                label="Days of Week"
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map(day => (
                      <Chip key={day} label={['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'][day]} size="small" />
                    ))}
                  </Box>
                )}
              >
                {[0, 1, 2, 3, 4, 5, 6].map(day => (
                  <MenuItem key={day} value={day}>
                    {['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'][day]}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        )}
        {newChore.recurrenceRule?.frequency === 'monthly' && (
          <Grid item xs={12} component="div">
            <TextField
              label="Day of Month"
              type="number"
              value={newChore.recurrenceRule?.dayOfMonth || ''}
              onChange={(e) => updateRecurrenceField('dayOfMonth', parseInt(e.target.value) || undefined)}
              fullWidth
              InputProps={{ inputProps: { min: 1, max: 31 } }}
            />
          </Grid>
        )}
        {newChore.recurrenceRule?.frequency === 'yearly' && (
          <>
            <Grid item xs={6} component="div">
              <FormControl fullWidth>
                <InputLabel>Month</InputLabel>
                <Select
                  value={newChore.recurrenceRule?.month || ''}
                  onChange={(e) => updateRecurrenceField('month', e.target.value)}
                  label="Month"
                >
                  {[
                    'January', 'February', 'March', 'April', 'May', 'June',
                    'July', 'August', 'September', 'October', 'November', 'December'
                  ].map((month, index) => (
                    <MenuItem key={index} value={index}>
                      {month}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={6} component="div">
              <TextField
                label="Day of Month"
                type="number"
                value={newChore.recurrenceRule?.dayOfMonth || ''}
                onChange={(e) => updateRecurrenceField('dayOfMonth', parseInt(e.target.value) || undefined)}
                fullWidth
                InputProps={{ inputProps: { min: 1, max: 31 } }}
              />
            </Grid>
          </>
        )}
      </Grid>
      {newChore.isRecurring && newChore.recurrenceRule && (
        <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
          {getRecurrenceDescription(newChore.recurrenceRule)}
        </Typography>
      )}
    </Box>
  );

  const renderChoreDialog = (isEdit: boolean) => (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Dialog open={isEdit ? isEditDialogOpen : isAddDialogOpen} onClose={isEdit ? handleCloseEditDialog : handleCloseAddDialog} maxWidth="md" fullWidth>
        <DialogTitle>{isEdit ? 'Edit Chore' : 'Add New Chore'}</DialogTitle>
        <DialogContent>
          <Box sx={{ pt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              label="Title"
              value={newChore.title || ''}
              onChange={(e) => updateNewChoreField('title', e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="Description"
              value={newChore.description || ''}
              onChange={(e) => updateNewChoreField('description', e.target.value)}
              multiline
              rows={3}
              fullWidth
            />
            <Grid container spacing={2}>
              <Grid item xs={6} component="div">
                <DatePicker
                  label="Start Date"
                  value={newChore.start || new Date()}
                  onChange={(date) => updateNewChoreField('start', date)}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>
              <Grid item xs={6} component="div">
                <TimePicker
                  label="Start Time"
                  value={newChore.start || new Date()}
                  onChange={(date) => updateNewChoreField('start', date)}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>
              <Grid item xs={6} component="div">
                <DatePicker
                  label="End Date"
                  value={newChore.end || new Date(Date.now() + 60 * 60 * 1000)}
                  onChange={(date) => updateNewChoreField('end', date)}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>
              <Grid item xs={6} component="div">
                <TimePicker
                  label="End Time"
                  value={newChore.end || new Date(Date.now() + 60 * 60 * 1000)}
                  onChange={(date) => updateNewChoreField('end', date)}
                  slotProps={{ textField: { fullWidth: true } }}
                />
              </Grid>
            </Grid>
            <FormControl fullWidth>
              <InputLabel>Assigned To</InputLabel>
              <Select
                multiple
                value={newChore.assignedTo || []}
                onChange={(e) => updateNewChoreField('assignedTo', e.target.value)}
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
            <FormControlLabel
              control={
                <Switch
                  checked={newChore.isRecurring || false}
                  onChange={(e) => updateNewChoreField('isRecurring', e.target.checked)}
                />
              }
              label="Make this chore recurring"
            />
            {newChore.isRecurring && renderRecurrenceSettings()}
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={isEdit ? handleCloseEditDialog : handleCloseAddDialog}>Cancel</Button>
          <Button
            onClick={isEdit ? handleUpdateChore : handleAddChore}
            variant="contained"
            disabled={!isChoreValid()}
          >
            {isEdit ? 'Update Chore' : 'Add Chore'}
          </Button>
        </DialogActions>
      </Dialog>
    </LocalizationProvider>
  );

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5">Manage Chores</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleOpenAddDialog}
        >
          Add New Chore
        </Button>
      </Box>

      <Paper elevation={2} sx={{ mb: 3 }}>
        <Tabs value={activeTab} onChange={(_, newValue) => setActiveTab(newValue)}>
          <Tab label={`All Chores (${chores.length})`} />
          <Tab label="Recurring" />
          <Tab label="Assigned to Me" />
        </Tabs>
      </Paper>

      <Grid container spacing={3}>
        {chores
          .filter(chore => {
            if (activeTab === 1) return chore.isRecurring;
            if (activeTab === 2) return chore.assignedTo.length > 0; // TODO: filter by current user
            return true;
          })
          .map(chore => (
            <Grid item xs={12} md={6} lg={4} key={chore.id} component="div">
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
                    <Typography variant="h6" component="div">
                      {chore.title}
                    </Typography>
                    {chore.isRecurring && (
                      <Chip
                        icon={<ScheduleIcon />}
                        label="Recurring"
                        size="small"
                        color="primary"
                        variant="outlined"
                      />
                    )}
                  </Box>
                  <Typography variant="body2" color="text.secondary" paragraph>
                    {chore.description || 'No description'}
                  </Typography>
                  <Typography variant="body2" paragraph>
                    <strong>Time:</strong> {new Date(chore.start).toLocaleString()} - {new Date(chore.end).toLocaleTimeString()}
                  </Typography>
                  {chore.isRecurring && chore.recurrenceRule && (
                    <Typography variant="body2" paragraph>
                      <strong>Recurrence:</strong> {getRecurrenceDescription(chore.recurrenceRule)}
                    </Typography>
                  )}
                  <Box sx={{ mt: 2 }}>
                    <Typography variant="body2" gutterBottom>
                      <strong>Assigned to:</strong>
                    </Typography>
                    <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                      {chore.assignedTo.length > 0 ? (
                        chore.assignedTo.map(memberId => {
                          const member = teamMembers.find(m => m.id === memberId);
                          return member ? (
                            <Chip
                              key={memberId}
                              icon={<PersonIcon />}
                              label={member.name}
                              size="small"
                              sx={{ backgroundColor: member.color || '#e0e0e0' }}
                            />
                          ) : null;
                        })
                      ) : (
                        <Typography variant="body2" color="text.secondary">
                          Unassigned
                        </Typography>
                      )}
                    </Box>
                  </Box>
                </CardContent>
                <CardActions>
                  <Button
                    size="small"
                    startIcon={<EditIcon />}
                    onClick={() => handleOpenEditDialog(chore)}
                  >
                    Edit
                  </Button>
                  <Button
                    size="small"
                    startIcon={<DeleteIcon />}
                    onClick={() => handleDeleteChore(chore.id)}
                    color="error"
                  >
                    Delete
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
      </Grid>

      {chores.length === 0 && (
        <Paper elevation={2} sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="body1" color="text.secondary">
            No chores yet. Click "Add New Chore" to create your first chore.
          </Typography>
        </Paper>
      )}

      {renderChoreDialog(false)}
      {renderChoreDialog(true)}
    </Box>
  );
};

export default ChoreManager;