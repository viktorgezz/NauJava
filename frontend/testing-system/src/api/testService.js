/**
 * Сервис для работы с API тестов.
 * Инкапсулирует все запросы связанные с получением списка тестов и поиском по названию.
 */
import apiClient from './config'
import { extractPagedData } from '@/utils/hateoasParser'

/**
 * Получает список тестов с пагинацией
 * @param {Object} params - Параметры пагинации
 * @param {number} params.page - Номер страницы (начиная с 0)
 * @param {number} params.size - Размер страницы (по умолчанию 20)
 * @param {string} params.sort - Параметры сортировки (например, "id,DESC")
 * @param {boolean} params.onlyMyTests - Показывать только свои тесты (по умолчанию false)
 * @returns {Promise<Object>} Страница с тестами
 */
export const getTests = async (params = {}) => {
  const { page = 0, size = 20, sort = 'id,DESC', onlyMyTests = false } = params
  const response = await apiClient.get('/tests', {
    params: {
      page,
      size,
      sort,
      onlyMyTests,
    },
  })
  return extractPagedData(response.data)
}

/**
 * Получает тесты по названию с пагинацией
 * @param {string} title - Название для поиска
 * @param {Object} params - Параметры пагинации
 * @param {number} params.page - Номер страницы (начиная с 0)
 * @param {number} params.size - Размер страницы (по умолчанию 20)
 * @param {string} params.sort - Параметры сортировки (по умолчанию "title,ASC")
 * @param {boolean} params.onlyMyTests - Показывать только свои тесты (по умолчанию false)
 * @returns {Promise<Object>} Страница с тестами
 */
export const getTestsByTitle = async (title, params = {}) => {
  const { page = 0, size = 20, sort = 'title,ASC', onlyMyTests = false } = params
  const response = await apiClient.get('/tests/title', {
    params: {
      title,
      page,
      size,
      sort,
      onlyMyTests,
    },
  })
  return extractPagedData(response.data)
}

/**
 * Получает информацию о тесте по ID
 * @param {number} testId - ID теста
 * @returns {Promise<Object>} Данные теста
 */
export const getTestById = async (testId) => {
  const response = await apiClient.get('/tests/metadata', {
    params: { id: testId },
  })
  return response.data
}

/**
 * Получает содержимое теста для редактирования
 * @param {number} testId - ID теста
 * @returns {Promise<Object>} Содержимое теста (TestUpdateTestContentDto)
 */
export const getTestContent = async (testId) => {
  const response = await apiClient.get('/tests/content', {
    params: { id: testId },
  })
  return response.data
}

/**
 * Обновляет содержимое теста
 * @param {Object} testContent - Содержимое теста (TestUpdateTestContentDto)
 * @returns {Promise<void>}
 */
export const updateTestContent = async (testContent) => {
  await apiClient.put('/tests/content', testContent)
}

/**
 * Создает новый тест с метаданными
 * @param {Object} testData - Данные теста
 * @param {string} testData.title - Название теста (обязательное)
 * @param {string} testData.description - Описание теста (опциональное)
 * @param {string} testData.status - Статус теста (PRIVATE, PUBLIC, UNLISTED)
 * @param {Array<string>} testData.titlesTopic - Список названий тем (от 0 до 5)
 * @returns {Promise<number>} ID созданного теста
 */
export const createTestMetadata = async (testData) => {
  const response = await apiClient.post('/tests/metadata', {
    idTest: null,
    title: testData.title,
    description: testData.description || null,
    status: testData.status,
    titlesTopic: testData.titlesTopic || [],
  })
  return response.data
}

/**
 * Обновляет метаданные теста
 * @param {Object} testData - Данные теста
 * @param {number} testData.idTest - ID теста (обязательное для обновления)
 * @param {string} testData.title - Название теста (обязательное)
 * @param {string} testData.description - Описание теста (опциональное)
 * @param {string} testData.status - Статус теста (PRIVATE, PUBLIC, UNLISTED)
 * @param {Array<string>} testData.titlesTopic - Список названий тем (от 0 до 5)
 * @returns {Promise<number>} ID обновленного теста
 */
export const updateTestMetadata = async (testData) => {
  const response = await apiClient.post('/tests/metadata', {
    idTest: testData.idTest,
    title: testData.title,
    description: testData.description || null,
    status: testData.status,
    titlesTopic: testData.titlesTopic || [],
  })
  return response.data
}

/**
 * Получает содержимое теста для прохождения
 * @param {number} testId - ID теста
 * @returns {Promise<Object>} Содержимое теста (TestToPassDto)
 */
export const getTestForPassing = async (testId) => {
  const response = await apiClient.get(`/tests/${testId}`)
  return response.data
}

/**
 * Получает последние 3 попытки прохождения теста
 * @param {number} testId - ID теста
 * @returns {Promise<Array>} Список последних попыток (ResultShortMetadataResponseDto)
 */
export const getTestLastAttempts = async (testId) => {
  const response = await apiClient.get(`/tests/${testId}/results/last`)
  return response.data
}

/**
 * Удаляет тест по ID
 * @param {number} testId - ID теста
 * @returns {Promise<void>}
 */
export const deleteTest = async (testId) => {
  await apiClient.delete(`/tests/${testId}`)
}

/**
 * Обновляет содержимое теста через JSON
 * @param {number} testId - ID теста
 * @param {string} testJson - JSON строка с содержимым теста
 * @returns {Promise<void>}
 */
export const updateTestContentJson = async (testId, testJson) => {
  // Парсим JSON и устанавливаем idTest
  const jsonData = JSON.parse(testJson)
  jsonData.idTest = testId
  const jsonString = JSON.stringify(jsonData)
  
  await apiClient.put('/tests/content/json', jsonString, {
    headers: {
      'Content-Type': 'text/plain',
    },
  })
}
