package tk.dzrcc.selenium.service;

import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import tk.dzrcc.selenium.WardenGame;
import tk.dzrcc.selenium.stages.HintStatus;
import tk.dzrcc.selenium.stages.SpoilerStatus;
import tk.dzrcc.telebot.DozorBot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;
import java.util.regex.Matcher;

/**
 * Created by mazh0416 on 6/26/2017.
 */
public class WardenService implements Runnable{
    private static final String OPEN_SPOILER_MESSAGE = "Открыт спойлер! Сейчас будет скрин...";
    private static final String NEW_TASK_MESSAGE = "Выдано задание №";
    private static final String SPOILER_MESSAGE = "\nВ задании спойлер.";
    private WardenGame game;
    private WebDriver driver;
    private DozorBot bot;
    private boolean noTask = false;

    private String login;
    private String pass;
    private static Pattern pattern = Pattern.compile("Задание \\d+");
    private static final String LEVEL_STRING = "Время на уровне: ";
    private static Pattern patternTime = Pattern.compile(LEVEL_STRING + "\\d{2}:\\d{2}:\\d{2}");
    private static Pattern patternAdv = Pattern.compile("Подсказка .+" + LEVEL_STRING);
    private static Pattern patternGPS = Pattern.compile("\\d\\d\\.\\d{3,15}( |,) *\\d\\d\\.\\d{3,15}");
    private static Pattern patternURL = Pattern.compile("https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,3}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");

    public WardenService(String authLink, String login, String pass, String gameLogin, String gamePass, DozorBot bot) {
        this.login = login;
        this.pass = pass;
        this.bot = bot;
        this.game = new WardenGame(authLink, gameLogin, gamePass);
        System.setProperty("webdriver.gecko.driver", "C:\\Program Files (x86)\\gecko\\geckodriver.exe");
        this.driver = new FirefoxDriver();
        driver.manage().window().maximize();

        auth(game);

    }

    private void auth(WardenGame game) {
        System.out.println("auth");
        driver.get(game.getAuthLink());
        driver.findElement(By.name("login")).sendKeys(login);
        WebElement webElement = driver.findElement(By.name("password"));
        webElement.sendKeys(pass);
        webElement.submit();
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.findElement(By.className("grayBox")).getText().contains(login);
            }
        });

    }

    @Override
    public void run() {
        while (true) {
            System.out.println("execute " + game.getGameLink());
            driver.get(game.getGameLink());
            performTaskCheck(driver, game);
            try {
                Thread.sleep(10000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void performHintCheck(WebDriver driver, WardenGame game) {
        String page = driver.findElement(By.tagName("table")).getText();
        Matcher timeMatcher = patternTime.matcher(page);
        if (timeMatcher.find()) {
            String time = timeMatcher.group().replace(LEVEL_STRING, "");
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            Date date = null;
            try {
                date = sdf.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            java.util.Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(java.util.Calendar.HOUR);
            int min = calendar.get(java.util.Calendar.MINUTE);
            HintStatus currentHintStatus = game.getHintStatus();
            if (currentHintStatus.isTimeToChange(hour, min)) {

                if (currentHintStatus.equals(HintStatus.FIRST_HINT) || currentHintStatus.equals(HintStatus.SECOND_HINT)) {
                    Matcher matcher = patternAdv.matcher(page.replace("\n", " ").replace("\r", ""));
                    if (matcher.find()) {
                        String textAdv = matcher.group().replace(LEVEL_STRING, "");
                        bot.sendToTelegram(currentHintStatus.getMessage() + "\n\n" + textAdv);
                        System.out.println(textAdv);
                    }
                } else {
                    bot.sendToTelegram(currentHintStatus.getMessage());
                }
                game.setHintStatus(currentHintStatus.getNext());
            }

        }
    }

    private void performSpoilerCheck(WebDriver driver, WardenGame game, String taskNum) {
        if (game.getSpoilerStatus().equals(SpoilerStatus.NOT_PASSED_SPOILER) && !withSpoiler(driver)) {
            bot.sendToTelegram(OPEN_SPOILER_MESSAGE);
            sendScreenshot(driver, "spoiler" + taskNum);
            checkCoordinates(driver);
            game.setSpoilerStatus(SpoilerStatus.PASSED_SPOILER);
        }
    }

    private void performTaskCheck(WebDriver driver, WardenGame game) {
        List<WebElement> titles = driver.findElements(By.className("title"));
        String taskNum = null;
        for (WebElement title : titles) {
            if (title.getText().contains("Задание")) {
                java.util.regex.Matcher matcher = pattern.matcher(title.getText());

                if (matcher.find()) {
                    taskNum = matcher.group().replace("Задание ", "");
                }
                System.out.println("Задание: " + taskNum);
                break;
            }
        }

        if (taskNum != null) {
            noTask = false;
            if (!taskNum.equals(game.getTaskNumber())) {
                System.out.println("Новое задание");
                game.setTaskNumber(taskNum);
                game.setHintStatus(HintStatus.FIRST_HINT_3_MIN);
                boolean withSpoiler = withSpoiler(driver);
                game.setSpoilerStatus(withSpoiler ? SpoilerStatus.NOT_PASSED_SPOILER : SpoilerStatus.NO_SPOILER);
                notificateAboutNewTask(driver, taskNum, withSpoiler);

                checkURL(driver);
                checkCoordinates(driver);
            } else {
                performSpoilerCheck(driver, game, taskNum);
                performHintCheck(driver, game);
            }
        } else {
            if (!noTask) {
                bot.sendToAdmin("Не найдено задание");
                noTask = true;
            }
        }

    }

    private void checkCoordinates(WebDriver driver) {
        WebElement task;
        try {
            task = driver.findElement(By.className("zad"));
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return;
        }

        System.out.println(task.getText());
        Matcher matcher = patternGPS.matcher(task.getText());
        while (matcher.find()){
            String text =  matcher.group();
            System.out.println(text);
            String[] pos = text
                    .replaceAll("( +|,)", ",")
                    .replaceAll(",+",",")
                    .split(",");
            System.out.println(pos.length +" --- "+ pos);
            if (pos.length == 2) {
                Float lat = null;
                Float lng = null;
                try {
                    lat = Float.parseFloat(pos[0]);
                    lng = Float.parseFloat(pos[1]);
                    bot.sendToTelegram(text);
                    bot.sendToTelegram(lat, lng);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void checkURL(WebDriver driver) {
        WebElement task;
        try {
            task = driver.findElement(By.className("zad"));
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return;
        }

        Matcher matcher = patternURL.matcher(task.getText());
        while (matcher.find()){
            bot.sendLinkToTelegram("Ссылка в задании:\n"+matcher.group());
        }
    }

    private void notificateAboutNewTask(WebDriver driver, String taskNum, boolean withSpoiler) {
        String message = NEW_TASK_MESSAGE + taskNum;
        if (withSpoiler) {
            message += SPOILER_MESSAGE;
        }
        message += "\nСейчас будет скрин...";
        bot.sendToTelegram(message);
        sendScreenshot(driver, "task" + taskNum);
    }

    private void sendScreenshot(WebDriver driver, String name) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0)", "");
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        BufferedImage fullImg = null;
        try {
            fullImg = ImageIO.read(screenshot);

            WebElement ele = driver.findElement(By.className("zad"));
            WebElement msg = driver.findElement(By.className("sysmsg"));
            Point point = ele.getLocation();
            int posY = msg.getLocation().getY() + msg.getSize().getHeight();
            int dop = ele.getLocation().getY()-posY;
            int eleWidth = ele.getSize().getWidth();
            int eleHeight = ele.getSize().getHeight();

            BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), posY, eleWidth, eleHeight+dop);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(eleScreenshot, "png", os);

           /*
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(fullImg, "png", os);
            //--------*/

            InputStream is = new ByteArrayInputStream(os.toByteArray());
            bot.sendToTelegram(name, is);
            //FileUtils.copyFile(screenshot, new File("screenshot.png"));

        } catch (IOException | NoSuchElementException e) {
            e.printStackTrace();
        }
    }


    private boolean withSpoiler(WebDriver driver) {
        boolean withSpoiler = true;
        try {
            driver.findElement(By.name("spoilerCode"));
        } catch (NoSuchElementException e) {
            withSpoiler = false;
            System.out.println("No spoiler");
        }
        return withSpoiler;
    }
}
