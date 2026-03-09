/**
 * Сервис для работы с API результатов тестов.
 * Инкапсулирует запросы связанные с отправкой и получением результатов прохождения тестов.
 */
import apiClient from './config'
import { extractPagedData } from '@/utils/hateoasParser'

/**
 * Отправляет результат прохождения теста
 * @param {Object} resultData - Данные результата (ResultRqDto)
 * @param {number} resultData.timeSpentSeconds - Время прохождения теста в секундах
 * @param {number} resultData.idTest - ID теста
 * @param {Object} resultData.idQuestionToUserAnswers - Ответы пользователя (Map<Long, UserAnswerRequestDto>)
 * @returns {Promise<number>} ID созданного результата
 */
export const submitTestResult = async (resultData) => {
  const response = await apiClient.post('/results', resultData)
  return response.data
}

/**
 * Получает результат прохождения теста по ID
 * @param {number} resultId - ID результата
 * @returns {Promise<Object>} Данные результата (ResultRsDto)
 */
export const getTestResult = async (resultId) => {
  const response = await apiClient.get(`/results/${resultId}`)
  return response.data
}

/**
 * Получает список результатов пользователя с пагинацией
 * @param {Object} params - Параметры пагинации
 * @param {number} params.page - Номер страницы (начиная с 0)
 * @param {number} params.size - Размер страницы (по умолчанию 20)
 * @param {string} params.sort - Параметры сортировки (например, "completedAt,ASC")
 * @returns {Promise<Object>} Страница с результатами
 */
export const getUserResults = async (params = {}) => {
  const { page = 0, size = 20, sort = 'completedAt,DESC' } = params
  const response = await apiClient.get('/results', {
    params: {
      page,
      size,
      sort,
    },
  })
  return extractPagedData(response.data)
}
