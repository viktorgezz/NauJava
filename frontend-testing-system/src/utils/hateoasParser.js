/**
 * Утилиты для парсинга HATEOAS структур ответов от Spring HATEOAS API.
 * Отвечает за извлечение и нормализацию данных из PagedModel и EntityModel структур.
 */

/**
 * Извлекает данные из HATEOAS PagedModel структуры
 * @param {Object} hateoasResponse - Ответ в формате HATEOAS (PagedModel<EntityModel<T>>)
 * @returns {Object} Нормализованный объект с данными пагинации
 * @property {Array} content - Массив элементов
 * @property {number} totalPages - Общее количество страниц
 * @property {number} totalElements - Общее количество элементов
 * @property {number} number - Номер текущей страницы (0-based)
 * @property {number} size - Размер страницы
 * @property {boolean} first - Является ли это первой страницей
 * @property {boolean} last - Является ли это последней страницей
 * @property {Object} _links - HATEOAS ссылки для навигации
 */
export const extractPagedData = (response) => {
  let content = []

  // Новый формат: Spring Data PagedModel — content прямо в корне ответа
  if (Array.isArray(response.content)) {
    content = response.content
  } else {
    // Старый формат: Spring HATEOAS PagedModel — данные в _embedded
    const embedded = response._embedded || {}
    const embeddedKey = Object.keys(embedded)[0]
    if (embeddedKey && Array.isArray(embedded[embeddedKey])) {
      content = embedded[embeddedKey]
    }
  }

  // Информация о пагинации всегда приходит в поле page
  const pageInfo = response.page || {}

  return {
    content,
    totalPages: pageInfo.totalPages ?? 1,
    totalElements: pageInfo.totalElements ?? 0,
    number: pageInfo.number ?? 0,
    size: pageInfo.size ?? 20,
    first: (pageInfo.number ?? 0) === 0,
    last: (pageInfo.number ?? 0) >= (pageInfo.totalPages ?? 1) - 1,
    _links: response._links || {},
  }
}

/**
 * Извлекает данные из HATEOAS EntityModel структуры
 * @param {Object} entityModel - Объект в формате EntityModel<T>
 * @returns {Object} Извлеченные данные без HATEOAS обертки
 */
export const extractEntityData = (entityModel) => {
  if (!entityModel) {
    return null
  }

  // EntityModel содержит данные в корне объекта, а _links - отдельно
  // Копируем все свойства кроме _links
  // eslint-disable-next-line no-unused-vars
  const { _links, ...data } = entityModel
  return data
}
