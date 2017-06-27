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
    private static final String OPEN_SPOILER_MESSAGE = "Открыт спойлер! Ща пришлю скрин...";
    private static final String NEW_TASK_MESSAGE = "Выдано задание №";
    private static final String SPOILER_MESSAGE = "\nВ задании спойлер.";
    private WardenGame game;
    private WebDriver driver;
    private DozorBot bot;
    private boolean noTask = false;

    private static final String LOGIN = "Zubrrr";
    private static final String PASS = "121212";
    private static Pattern pattern = Pattern.compile("Задание \\d+");
    private static final String LEVEL_STRING = "Время на уровне: ";
    private static Pattern patternTime = Pattern.compile(LEVEL_STRING + "\\d{2}:\\d{2}:\\d{2}");
    private static Pattern patternAdv = Pattern.compile("Подсказка .+" + LEVEL_STRING);

    public WardenService(String authLink, String gameLogin, String gamePass, DozorBot bot) {
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
        driver.findElement(By.name("login")).sendKeys(LOGIN);
        WebElement webElement = driver.findElement(By.name("password"));
        webElement.sendKeys(PASS);
        webElement.submit();
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.findElement(By.className("grayBox")).getText().contains(LOGIN);
            }
        });

    }

    @Override
    public void run() {
        // TODO: 6/27/2017
        int i = 0;
        while (true) {
            System.out.println("execute");
            //driver.get(game.getGameLink());
            driver.get("http://dzrcc.tk/test/q/"+i+".html");
            i++;
            //driver.get(game.getAuthLink());
            //bot.sendToTelegram(game.getGameLink());
            // TODO: 6/27/2017  
            //sendScreenshot(driver, "image" + (new Random()).nextInt(100));

            // TODO: 6/26/2017
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
            java.util.Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);   // assigns calendar to given date
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

    private void notificateAboutNewTask(WebDriver driver, String taskNum, boolean withSpoiler) {
        String message = NEW_TASK_MESSAGE + taskNum;
        if (withSpoiler) {
            message += SPOILER_MESSAGE;
        }
        message += "\nЩа пришлю скрин...";
        bot.sendToTelegram(message);
        sendScreenshot(driver, "task" + taskNum);
    }

    private void sendScreenshot(WebDriver driver, String name) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0,0)", "");
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        BufferedImage fullImg = null;
        try {
            fullImg = ImageIO.read(screenshot);

            // TODO: 6/27/2017  
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

           /* // TODO: 6/26/2017
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(fullImg, "png", os);
            //--------*/

            InputStream is = new ByteArrayInputStream(os.toByteArray());
            bot.sendToTelegram(name, is);
            //FileUtils.copyFile(screenshot, new File("screenshot.png"));

        } catch (IOException e) {
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
