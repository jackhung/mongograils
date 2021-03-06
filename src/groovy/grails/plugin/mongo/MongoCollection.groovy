package grails.plugin.mongo

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author jack
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@interface MongoCollection {
        String value()
}

