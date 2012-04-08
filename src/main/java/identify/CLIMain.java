package identify;

import com.google.inject.Guice;
import com.google.inject.Injector;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CLIMain {
    private static void showHelp() {
        System.err.println("Please provide a destination path.");
    }

    public static void main(final String[] args) throws Exception {
        if (args.length == 1) {
            final Injector injector = Guice.createInjector(new IdentifyModule());
            System.out.println(injector.getInstance(TreeGeneratorFactory.class).getGenerator(args[0]).generate());
        } else {
            showHelp();
            System.exit(1);
        }
    }
}
