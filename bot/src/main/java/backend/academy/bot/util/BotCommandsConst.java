package backend.academy.bot.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class BotCommandsConst {

    public static final String REMEMBER_TO_REGISTER = "Для начала работы зарегистрируйтесь.";
    public static final String INTERNAL_ERROR = "Произошла ошибка на сервере. Попробуйте позже.";
    public static String helpDescription = ""; // динамически заполняется
    public static final String USER_ALREADY_REGISTERED = "Вы уже зарегистрированы";
    public static final String UNKNOWN_COMMAND = "Неизвестная команда!";
    public static final String REGISTER_SUCCESS = "Вы успешно зарегистрировались";

    public static final String START_COMMAND = "/start";
    public static final String START_COMMAND_DESCRIPTION = "Регистрация пользователя.";

    public static final String HELP_COMMAND = "/help";
    public static final String HELP_COMMAND_DESCRIPTION = "Вывести список доступных команд.";

    public static final String TRACK_COMMAND = "/track";
    public static final String TRACK_COMMAND_DESCRIPTION = "Начать отслеживание ссылки.";
    public static final String TRACK_COMMAND_TAGS = "Введите тэги (если не хотите, то введите 'нет')";
    public static final String TRACK_COMMAND_FILTERS = "Введите фильтры (если не хотите, то введите 'нет')";
    public static final String TRACK_COMMAND_WRONG_FILTERS = "Введены неверные фильтры - они будут пропущены";
    public static final String TRACK_COMMAND_SUCCESS = "Теперь эта ссылка будет отслеживаться";

    public static final String UNTRACK_COMMAND = "/untrack";
    public static final String UNTRACK_COMMAND_DESCRIPTION = "Прекратить отслеживание ссылки.";
    public static final String UNTRACK_COMMAND_WRONG_URI = "Введите корректный URI";

    public static final String LIST_COMMAND = "/list";
    public static final String LIST_COMMAND_DESCRIPTION = "Показать список отслеживаемых ссылок";
    public static final String LIST_COMMAND_EMPTY = "Нет ссылок для отслеживания";
}
