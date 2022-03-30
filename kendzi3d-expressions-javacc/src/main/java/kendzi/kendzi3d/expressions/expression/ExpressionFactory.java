package kendzi.kendzi3d.expressions.expression;

import java.util.List;

import kendzi.kendzi3d.expressions.Context;
import kendzi.kendzi3d.expressions.exeption.FunctionExeption;
import kendzi.kendzi3d.expressions.functions.Function;
import kendzi.kendzi3d.expressions.functions.OneParamFunction;
import kendzi.kendzi3d.expressions.functions.ZeroParamFunction;


public class ExpressionFactory {

    public static class DoubleExpresion implements Expression {
        private double value;
        public DoubleExpresion(double value) {
            this.value = value;
        }
        public Object evaluate(Context context) {
            return value;
        }
    }

    public static class NullExpresion implements Expression {
        public NullExpresion() {
            //
        }

        public Object evaluate(Context context) {
            return null;
        }
    }

    public static Expression fun(final String name, List<Expression> value) {
        if (value.size() == 0) {
            return fun(name);
        } else if (value.size() == 1) {
            return fun(name, value.get(0));
        }
        throw new RuntimeException("wrong num of params");
    }

    public static Expression fun(final String name, Expression value) {

        return new OneArgExpression<Double>(value, Double.class) {

            public Object evaluate(Context context) {
                if (context == null) {
                    throw new RuntimeException("can't find function " + name + " context is null");
                }
                if (context.getFunctions() == null) {
                    throw new RuntimeException("can't find function " + name + " context functions are null");
                }

                Function fun = context.getFunctions().get(name);
                if (fun == null) {
                    throw new FunctionExeption("can't find in context function with name: " + name);
                }
                if (fun instanceof OneParamFunction) {
                    Double param = convert(arg1, context);
                    //                    Object evaluate = value.evaluate(context);
                    return ((OneParamFunction) fun).evalOneParam(context, param);

                } else {
                    throw new RuntimeException("wrong number of parameters");
                }
            }
        };
    }

    public static Expression fun(final String name) {

        return new ArgExpression<Double>(Double.class) {

            public Object evaluate(Context context) {
                if (context == null) {
                    throw new RuntimeException("can't find function " + name + " context is null");
                }
                if (context.getFunctions() == null) {
                    throw new RuntimeException("can't find function " + name + " context functions are null");
                }

                Function fun = context.getFunctions().get(name);
                if (fun == null) {
                    throw new FunctionExeption("can't find in context function with name: " + name);
                }
                if (fun instanceof ZeroParamFunction) {
                    return ((ZeroParamFunction) fun).evalZeroParam(context);

                } else {
                    throw new RuntimeException("wrong number of parameters for function: " + name);
                }
            }
        };
    }

    public static Expression variable(final String name) {
        return new Expression() {
            public Object evaluate(Context context) {

                if (context == null) {
                    throw new RuntimeException("can't find variable " + name + " context is null");
                }
                if (context.getFunctions() == null) {
                    throw new RuntimeException("can't find variable " + name + " context variables are null");
                }

                Object ret = context.getVariables().get(name);
                if (ret instanceof Double) {
                    return doubleValue((Double) ret);
                }
                throw new RuntimeException("unsupported variable type: " + ret);
            }
        };
    }

    public static Expression doubleValue(double value) {
        return new DoubleExpresion(value);
    }

    public static Expression sum(Expression expr1, Expression expr2) {
        return new TwoArgExpression<Double>(expr1, expr2, double.class) {

            public Object evaluate(Context context) {
                return convert(arg1, context) + convert(arg2, context);
            }
        };
    }

    public static Expression multiply(Expression expr1, Expression expr2) {
        return new TwoArgExpression<Double>(expr1, expr2, double.class) {

            public Object evaluate(Context context) {
                return convert(arg1, context) * convert(arg2, context);
            }
        };
    }
    public static Expression divide(Expression expr1, Expression expr2) {
        return new TwoArgExpression<Double>(expr1, expr2, double.class) {

            public Object evaluate(Context context) {
                return convert(arg1, context) / convert(arg2, context);
            }
        };
    }
    public static Expression sub(Expression expr1, Expression expr2) {
        return new TwoArgExpression<Double>(expr1, expr2, double.class) {

            public Object evaluate(Context context) {

                return convert(arg1, context) - convert(arg2, context);
            }
        };
    }

}
