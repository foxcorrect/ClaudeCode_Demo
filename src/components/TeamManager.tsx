import React, { useState } from 'react';
import {
  Box,
  Paper,
  Typography,
  Button,
  TextField,
  Grid,
  Card,
  CardContent,
  CardActions,
  IconButton,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Avatar,
  LinearProgress,
  Tooltip,
  Divider,
} from '@mui/material';
import {
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Email as EmailIcon,
  Person as PersonIcon,
  Assignment as AssignmentIcon,
  ColorLens as ColorLensIcon,
} from '@mui/icons-material';
import type { TeamMember, Chore } from '../types';

interface TeamManagerProps {
  teamMembers: TeamMember[];
  chores: Chore[];
  onAddMember: (member: TeamMember) => void;
  onDeleteMember: (id: string) => void;
}

const TeamManager: React.FC<TeamManagerProps> = ({
  teamMembers,
  chores,
  onAddMember,
  onDeleteMember,
}) => {
  const [isAddDialogOpen, setIsAddDialogOpen] = useState(false);
  const [isEditDialogOpen, setIsEditDialogOpen] = useState(false);
  const [selectedMember, setSelectedMember] = useState<TeamMember | null>(null);
  const [newMember, setNewMember] = useState<Partial<TeamMember>>({
    name: '',
    email: '',
    color: '#3498db',
  });
  const [colorPickerOpen, setColorPickerOpen] = useState(false);

  const predefinedColors = [
    '#ff6b6b', '#4ecdc4', '#ffd166', '#06d6a0', '#118ab2',
    '#9b5de5', '#f15bb5', '#00bbf9', '#00f5d4', '#fee440',
  ];

  const handleOpenAddDialog = () => {
    setNewMember({
      name: '',
      email: '',
      color: predefinedColors[Math.floor(Math.random() * predefinedColors.length)],
    });
    setIsAddDialogOpen(true);
  };

  const handleCloseAddDialog = () => {
    setIsAddDialogOpen(false);
  };

  const handleOpenEditDialog = (member: TeamMember) => {
    setSelectedMember(member);
    setNewMember(member);
    setIsEditDialogOpen(true);
  };

  const handleCloseEditDialog = () => {
    setIsEditDialogOpen(false);
    setSelectedMember(null);
  };

  const handleAddMember = () => {
    const member: TeamMember = {
      id: Date.now().toString(),
      name: newMember.name!,
      email: newMember.email,
      color: newMember.color,
    };
    onAddMember(member);
    setIsAddDialogOpen(false);
  };

  const handleUpdateMember = () => {
    if (selectedMember) {
      // In a real app, you would have an update function
      // For now, we'll just close the dialog
      setIsEditDialogOpen(false);
      setSelectedMember(null);
    }
  };

  const handleDeleteMember = (id: string) => {
    const member = teamMembers.find(m => m.id === id);
    if (member) {
      const assignedChores = chores.filter(chore => chore.assignedTo.includes(id));
      if (assignedChores.length > 0) {
        const confirmMessage = `${member.name} is assigned to ${assignedChores.length} chore(s).\n\nThese chores will become unassigned.\n\nAre you sure you want to remove this team member?`;
        if (window.confirm(confirmMessage)) {
          onDeleteMember(id);
        }
      } else {
        if (window.confirm(`Are you sure you want to remove ${member.name} from the team?`)) {
          onDeleteMember(id);
        }
      }
    }
  };

  const getMemberChores = (memberId: string) => {
    return chores.filter(chore => chore.assignedTo.includes(memberId));
  };

  const getMemberChoreCount = (memberId: string) => {
    return getMemberChores(memberId).length;
  };

  const getUpcomingChores = (memberId: string) => {
    const memberChores = getMemberChores(memberId);
    const now = new Date();
    return memberChores.filter(chore => new Date(chore.start) > now).length;
  };

  const isMemberValid = () => {
    return !!newMember.name?.trim();
  };

  const renderMemberDialog = (isEdit: boolean) => (
    <Dialog open={isEdit ? isEditDialogOpen : isAddDialogOpen} onClose={isEdit ? handleCloseEditDialog : handleCloseAddDialog} maxWidth="sm" fullWidth>
      <DialogTitle>{isEdit ? 'Edit Team Member' : 'Add Team Member'}</DialogTitle>
      <DialogContent>
        <Box sx={{ pt: 2, display: 'flex', flexDirection: 'column', gap: 2 }}>
          <TextField
            label="Name"
            value={newMember.name || ''}
            onChange={(e) => setNewMember({ ...newMember, name: e.target.value })}
            fullWidth
            required
          />
          <TextField
            label="Email"
            value={newMember.email || ''}
            onChange={(e) => setNewMember({ ...newMember, email: e.target.value })}
            fullWidth
            type="email"
          />
          <Box>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Color (for calendar display)
            </Typography>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box
                sx={{
                  width: 40,
                  height: 40,
                  borderRadius: '50%',
                  backgroundColor: newMember.color,
                  border: '2px solid #ccc',
                  cursor: 'pointer',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                }}
                onClick={() => setColorPickerOpen(!colorPickerOpen)}
              >
                <ColorLensIcon sx={{ color: 'white', fontSize: 20 }} />
              </Box>
              <TextField
                value={newMember.color || ''}
                onChange={(e) => setNewMember({ ...newMember, color: e.target.value })}
                size="small"
                sx={{ flexGrow: 1 }}
              />
            </Box>
            {colorPickerOpen && (
              <Box sx={{ mt: 2, display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {predefinedColors.map(color => (
                  <Box
                    key={color}
                    sx={{
                      width: 30,
                      height: 30,
                      borderRadius: '50%',
                      backgroundColor: color,
                      cursor: 'pointer',
                      border: color === newMember.color ? '2px solid #333' : '1px solid #ccc',
                    }}
                    onClick={() => {
                      setNewMember({ ...newMember, color });
                      setColorPickerOpen(false);
                    }}
                  />
                ))}
              </Box>
            )}
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={isEdit ? handleCloseEditDialog : handleCloseAddDialog}>Cancel</Button>
        <Button
          onClick={isEdit ? handleUpdateMember : handleAddMember}
          variant="contained"
          disabled={!isMemberValid()}
        >
          {isEdit ? 'Update Member' : 'Add Member'}
        </Button>
      </DialogActions>
    </Dialog>
  );

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Box>
          <Typography variant="h5">Manage Team</Typography>
          <Typography variant="body2" color="text.secondary">
            {teamMembers.length} team members • {chores.length} total chores
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleOpenAddDialog}
        >
          Add Team Member
        </Button>
      </Box>

      <Grid container spacing={3}>
        {teamMembers.map(member => {
          const choreCount = getMemberChoreCount(member.id);
          const upcomingCount = getUpcomingChores(member.id);
          const memberChores = getMemberChores(member.id);

          return (
            <Grid item xs={12} md={6} lg={4} key={member.id}>
              <Card>
                <CardContent>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Avatar
                      sx={{
                        bgcolor: member.color,
                        width: 56,
                        height: 56,
                        mr: 2,
                      }}
                    >
                      {member.name.charAt(0).toUpperCase()}
                    </Avatar>
                    <Box sx={{ flexGrow: 1 }}>
                      <Typography variant="h6" component="div">
                        {member.name}
                      </Typography>
                      {member.email && (
                        <Typography variant="body2" color="text.secondary" sx={{ display: 'flex', alignItems: 'center' }}>
                          <EmailIcon sx={{ fontSize: 14, mr: 0.5 }} />
                          {member.email}
                        </Typography>
                      )}
                    </Box>
                    <Box>
                      <Tooltip title="Edit">
                        <IconButton onClick={() => handleOpenEditDialog(member)} size="small">
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Delete">
                        <IconButton onClick={() => handleDeleteMember(member.id)} size="small" color="error">
                          <DeleteIcon />
                        </IconButton>
                      </Tooltip>
                    </Box>
                  </Box>

                  <Box sx={{ mt: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">
                        <AssignmentIcon sx={{ fontSize: 14, mr: 0.5 }} />
                        Assigned Chores
                      </Typography>
                      <Typography variant="body2" fontWeight="bold">
                        {choreCount}
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={choreCount > 0 ? Math.min((choreCount / 10) * 100, 100) : 0}
                      sx={{
                        height: 8,
                        borderRadius: 4,
                        mb: 2,
                        bgcolor: 'grey.200',
                        '& .MuiLinearProgress-bar': {
                          bgcolor: member.color,
                        },
                      }}
                    />
                  </Box>

                  {choreCount > 0 && (
                    <>
                      <Typography variant="body2" color="text.secondary" gutterBottom>
                        Upcoming: {upcomingCount} chore{upcomingCount !== 1 ? 's' : ''}
                      </Typography>
                      <Box sx={{ mt: 2 }}>
                        <Typography variant="body2" gutterBottom>
                          Assigned to:
                        </Typography>
                        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                          {memberChores.slice(0, 3).map(chore => (
                            <Tooltip key={chore.id} title={chore.description || chore.title}>
                              <Chip
                                label={chore.title}
                                size="small"
                                sx={{
                                  backgroundColor: member.color,
                                  color: 'white',
                                  maxWidth: 120,
                                }}
                              />
                            </Tooltip>
                          ))}
                          {memberChores.length > 3 && (
                            <Chip
                              label={`+${memberChores.length - 3} more`}
                              size="small"
                              variant="outlined"
                            />
                          )}
                        </Box>
                      </Box>
                    </>
                  )}

                  {choreCount === 0 && (
                    <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic' }}>
                      No chores assigned yet
                    </Typography>
                  )}
                </CardContent>
                <CardActions>
                  <Button
                    size="small"
                    startIcon={<PersonIcon />}
                    onClick={() => {
                      // TODO: Show member details modal
                    }}
                  >
                    View Details
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      {teamMembers.length === 0 && (
        <Paper elevation={2} sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="body1" color="text.secondary">
            No team members yet. Click "Add Team Member" to add your first team member.
          </Typography>
        </Paper>
      )}

      {/* Team Statistics */}
      {teamMembers.length > 0 && (
        <Paper elevation={2} sx={{ p: 3, mt: 4 }}>
          <Typography variant="h6" gutterBottom>
            Team Statistics
          </Typography>
          <Grid container spacing={3}>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h3" color="primary">
                  {teamMembers.length}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Team Members
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h3" color="primary">
                  {chores.length}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Total Chores
                </Typography>
              </Box>
            </Grid>
            <Grid item xs={12} md={4}>
              <Box sx={{ textAlign: 'center' }}>
                <Typography variant="h3" color="primary">
                  {chores.filter(c => c.isRecurring).length}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Recurring Chores
                </Typography>
              </Box>
            </Grid>
          </Grid>
        </Paper>
      )}

      {renderMemberDialog(false)}
      {renderMemberDialog(true)}
    </Box>
  );
};

export default TeamManager;