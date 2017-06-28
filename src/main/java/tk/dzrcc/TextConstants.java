package tk.dzrcc;

/**
 * Created by Maksim on 29.03.2017.
 */
public interface TextConstants {
    String TAKEN_CODE = "✅";
    String NOT_TAKEN_CODE = "❌";

    String CANNOT_INPUT_CODE = "Ошибка. Не получается вбить код.\n\n¯ \\ _ (ツ) _ / ¯";
    String CANNOT_LOAD_PAGE = "Что-то не получется загрузить страницу движка...\n\n¯ \\ _ (ツ) _ / ¯";
    String CANNOT_FIND_TASK_NUMBER = "На движке задания не обнаружено.\n\n¯ \\ _ (ツ) _ / ¯";
    String CANNOT_LOAD_LOGIN_PAGE = "Ошибка. Не получается загрузить страницу для ввода логина и пароля пользователя. Возможно, указан неправильный адрес.";
    String CANNOT_READ_CODE_PAGE = "Ошибка. Не получается прочитать страницу движка. Возможно, указан неправильный адрес.";
    String CANNOT_READ_SYSMESSAGE = "Ошибочка... Не получается прочитать сообщение от движка. Возможно, указан неправильный адрес.";
    String CANNOT_READ_ENGINE_INFO = "Ошибка. Не получается прочитать информацию на движке. Возможно, указан неправильный адрес.";
    String SUCCESS_AUTH = "Авторизация прошла успешно.\n";
    String SUCCESS_LOADED = "Задание загружено. К бою готов!\uD83D\uDCAA";

    String NO_CONNECTION = "Бот еще не подключен!\uD83D\uDE49";
    String HELP_TEXT = "*FAQ* Как пользоваться ботом\n\n" +
            "*Как вбить код?*\n" +
            "Просто отправляешь его в чат. Например, 12345. Любое число в чате бот воспримет как код и вобьет его в движок. В ответ получишь сообщение с движка (принят/не принят код) и подробную информацию о нем (сектор, код опасности, порядковый номер).\n\n" +
            "*А что, если код не только из цифр?*\n" +
            "Тогда нужно использовать команду /вбей. Например,\n«/вбей abc123». Без ковычек.\n\n" +
            "*Какие еще команды есть?*\n" +
            "/status – полная инфа по заданию (сколько и какие коды вбиты);\n" +
            "/time – время на уровне;\n" +
            "/pause – приостановка вбивания кодов (на случай ложных кодов в задании); \n" +
            "/resume – возобновление вбивания кодов.\n\n" +
            "*А че еще могёт?* \n" +
            "Постоянно обновляет движок и чекает что и как там с заданием. Оповещает о новом задании, об открытом спойлере, информирует, сколько минут до подсказки или слива (5, 3 или 1 мин), присылает подсказку. Помимо этого, если в выданном задании есть координаты, он пришлет вам точку в чат. А если есть ссылки, то и их тоже. Удобно – ебнисся. \n\n" +
            "*А если сломается?* \n" +
            "Да, такое может быть. Но ничего страшного, пользуйся движком как и раньше. Если вбиваешь код и бот ничего не отвечает, то скорее всего код не был вбит в движок и что-то пошло не так. А если бот ответил, то все ОК, сообщение он скопировал с движка. Такого, что бот говорит мол код вбит/не вбит, а на деле это не так – быть не может. \n\n" +
            "*С бонусными заданиями че?* \n" +
            "Ниче. С ними через движок. \n\n" +
            "*А я могу себе отдельно добавить бота в контакты и через свой диалог херачить?* \n" +
            "А ты хитрец. Но нет. Бот общается только с одним групповым чатом.";
    String INIT_CONNECTION = "Подключаюсь...";

    String PAUSE_MESSAGE = "Прием кодов остановлен. Для возобновления воспользуйтесь комндой /resume.";
    String RESUME_MESSAGE = "Прием кодов возобновлен!\nНо если что, жми /pause.";
    String GAME_IN_PAUSE = "Придержи коней. Прием кодов пока остановлен. Для возобновления жми /resume.";
}
