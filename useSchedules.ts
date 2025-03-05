import { useState, useCallback } from 'react';
import {
  getSchedules,
  getScheduleById,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  getSchedulesForTemplate,
  getSchedulesByStatus,
  activateSchedule,
  pauseSchedule,
  executeScheduleNow,
  getNextExecutionTimes,
  GenerationSchedule,
  ScheduleListParams,
  ScheduleListResponse
} from '../api/scheduleApi';
import useApi from './useApi';

const useSchedules = () => {
  const [currentSchedule, setCurrentSchedule] = useState<GenerationSchedule | null>(null);
  
  // Get all schedules with pagination
  const {
    data: schedulesData,
    isLoading: isLoadingSchedules,
    error: schedulesError,
    execute: fetchSchedules
  } = useApi<ScheduleListResponse, [ScheduleListParams?]>(getSchedules);
  
  // Get a schedule by ID
  const {
    data: scheduleData,
    isLoading: isLoadingSchedule,
    error: scheduleError,
    execute: fetchScheduleById
  } = useApi<GenerationSchedule, [number]>(getScheduleById);
  
  // Create a new schedule
  const {
    data: createData,
    isLoading: isCreating,
    error: createError,
    execute: createScheduleExecute
  } = useApi<GenerationSchedule, [GenerationSchedule]>(createSchedule);
  
  // Update an existing schedule
  const {
    data: updateData,
    isLoading: isUpdating,
    error: updateError,
    execute: updateScheduleExecute
  } = useApi<GenerationSchedule, [number, GenerationSchedule]>(updateSchedule);
  
  // Delete a schedule
  const {
    data: deleteData,
    isLoading: isDeleting,
    error: deleteError,
    execute: deleteScheduleExecute
  } = useApi<void, [number]>(deleteSchedule);
  
  // Get schedules for a template
  const {
    data: templateSchedulesData,
    isLoading: isLoadingTemplateSchedules,
    error: templateSchedulesError,
    execute: fetchSchedulesForTemplate
  } = useApi<GenerationSchedule[], [number]>(getSchedulesForTemplate);
  
  // Get schedules by status
  const {
    data: statusSchedulesData,
    isLoading: isLoadingStatusSchedules,
    error: statusSchedulesError,
    execute: fetchSchedulesByStatus
  } = useApi<GenerationSchedule[], [string]>(getSchedulesByStatus);
  
  // Activate a schedule
  const {
    data: activateData,
    isLoading: isActivating,
    error: activateError,
    execute: activateScheduleExecute
  } = useApi<GenerationSchedule, [number]>(activateSchedule);
  
  // Pause a schedule
  const {
    data: pauseData,
    isLoading: isPausing,
    error: pauseError,
    execute: pauseScheduleExecute
  } = useApi<GenerationSchedule, [number]>(pauseSchedule);
  
  // Execute a schedule immediately
  const {
    data: executeData,
    isLoading: isExecuting,
    error: executeError,
    execute: executeScheduleNowExecute
  } = useApi<void, [number]>(executeScheduleNow);
  
  // Get next execution times for a cron expression
  const {
    data: nextExecutionsData,
    isLoading: isLoadingNextExecutions,
    error: nextExecutionsError,
    execute: getNextExecutionTimesExecute
  } = useApi<string[], [string, number?]>(getNextExecutionTimes);
  
  // Helper function to load a schedule and set as current
  const loadScheduleById = useCallback(async (id: number) => {
    const response = await fetchScheduleById(id);
    if (response.data) {
      setCurrentSchedule(response.data);
    }
    return response;
  }, [fetchScheduleById]);
  
  // Create a schedule and set as current if successful
  const createNewSchedule = useCallback(async (schedule: GenerationSchedule) => {
    const response = await createScheduleExecute(schedule);
    if (response.data) {
      setCurrentSchedule(response.data);
    }
    return response;
  }, [createScheduleExecute]);
  
  // Update a schedule and update current if it's the same
  const updateExistingSchedule = useCallback(async (id: number, schedule: GenerationSchedule) => {
    const response = await updateScheduleExecute(id, schedule);
    if (response.data && currentSchedule && currentSchedule.id === id) {
      setCurrentSchedule(response.data);
    }
    return response;
  }, [updateScheduleExecute, currentSchedule]);
  
  // Delete a schedule and clear current if it's the same
  const deleteExistingSchedule = useCallback(async (id: number) => {
    const response = await deleteScheduleExecute(id);
    if (!response.error && currentSchedule && currentSchedule.id === id) {
      setCurrentSchedule(null);
    }
    return response;
  }, [deleteScheduleExecute, currentSchedule]);
  
  // Activate a schedule and update current if it's the same
  const activateExistingSchedule = useCallback(async (id: number) => {
    const response = await activateScheduleExecute(id);
    if (response.data && currentSchedule && currentSchedule.id === id) {
      setCurrentSchedule(response.data);
    }
    return response;
  }, [activateScheduleExecute, currentSchedule]);
  
  // Pause a schedule and update current if it's the same
  const pauseExistingSchedule = useCallback(async (id: number) => {
    const response = await pauseScheduleExecute(id);
    if (response.data && currentSchedule && currentSchedule.id === id) {
      setCurrentSchedule(response.data);
    }
    return response;
  }, [pauseScheduleExecute, currentSchedule]);
  
  // Execute a schedule immediately
  const executeScheduleImmediately = useCallback(async (id: number) => {
    return await executeScheduleNowExecute(id);
  }, [executeScheduleNowExecute]);
  
  return {
    // State
    schedules: schedulesData?.data?.content || [],
    currentSchedule,
    totalCount: schedulesData?.data?.totalElements || 0,
    totalPages: schedulesData?.data?.totalPages || 0,
    templateSchedules: templateSchedulesData?.data || [],
    statusSchedules: statusSchedulesData?.data || [],
    nextExecutionTimes: nextExecutionsData?.data || [],
    
    // Loading states
    isLoadingSchedules,
    isLoadingSchedule,
    isLoadingTemplateSchedules,
    isLoadingStatusSchedules,
    isLoadingNextExecutions,
    isCreating,
    isUpdating,
    isDeleting,
    isActivating,
    isPausing,
    isExecuting,
    
    // Errors
    schedulesError,
    scheduleError,
    templateSchedulesError,
    statusSchedulesError,
    nextExecutionsError,
    createError,
    updateError,
    deleteError,
    activateError,
    pauseError,
    executeError,
    
    // Actions
    fetchSchedules,
    loadScheduleById,
    createSchedule: createNewSchedule,
    updateSchedule: updateExistingSchedule,
    deleteSchedule: deleteExistingSchedule,
    fetchSchedulesForTemplate,
    fetchSchedulesByStatus,
    activateSchedule: activateExistingSchedule,
    pauseSchedule: pauseExistingSchedule,
    executeSchedule: executeScheduleImmediately,
    getNextExecutionTimes: getNextExecutionTimesExecute,
    setCurrentSchedule
  };
};

export default useSchedules;
