package com.pat.ged;

import com.pat.ged.service.FileDocumentService;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

public class FileDocumentServiceParameterResolver  implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return (parameterContext.getParameter().getType() == FileDocumentService.class);
    }

    @Override
    public FileDocumentService resolveParameter(ParameterContext parameterContext,
                                       ExtensionContext extensionContext) {
        return new FileDocumentService();
    }

}
