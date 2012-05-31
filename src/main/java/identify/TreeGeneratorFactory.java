package identify;

public interface TreeGeneratorFactory {
    abstract TreeGenerator getGenerator(String path) throws ConfigProperties.ConfigLoadingException;
}
