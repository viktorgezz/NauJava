/**
 * Сервис для работы с API результатов тестов.
 * Инкапсулирует запросы связанные с отправкой и получением результатов прохождения тестов.
 */
import apiClient from './config'

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

