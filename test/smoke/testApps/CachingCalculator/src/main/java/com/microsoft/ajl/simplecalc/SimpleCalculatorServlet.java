package com.microsoft.ajl.simplecalc;

import com.microsoft.ajl.simplecalc.model.BinaryCalculation;
import com.microsoft.ajl.simplecalc.model.BinaryOperator;
import com.microsoft.applicationinsights.TelemetryClient;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import redis.clients.jedis.Jedis;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import static com.microsoft.ajl.simplecalc.ParameterConstants.*;

@WebServlet(description = "Performs given calculation", urlPatterns = { "/doCalc" })
public class SimpleCalculatorServlet extends HttpServlet {

    private Jedis redis;

    public SimpleCalculatorServlet() {
        try {
            String redisHostname = System.getenv("REDIS");
            if (redisHostname != null) redis = new Jedis(redisHostname, 6379);
        } catch (Exception e) {
            System.err.println("Error with redis in servlet");
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        BinaryCalculation bc;
        try {
            bc = readParameters(request.getParameterMap());
        }
        catch (CalculatorParameterException cpe) {
            String errMsg = cpe.getLocalizedMessage();
            System.err.println(errMsg);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, errMsg);
            return;
        }

        if (bc == null) {
            System.out.println("No parameters given.");
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        response.setContentType("text/html;charset=UTF-8");
        renderHtml(bc, response.getWriter());

        new TelemetryClient().trackMetric("TimeToRespond", 111222333);
    }

    /**
     * @param parameterMap
     * @return null if parameterMap is empty
     */
    private BinaryCalculation readParameters(Map<String, String[]> parameterMap) throws CalculatorParameterException {
        if (parameterMap == null) {
            throw new IllegalArgumentException("parameterMap cannot be null");
        }

        if (parameterMap.isEmpty()) {
            return null;
        }

        // log params
        System.out.println("Given parameters:");
        for (Entry<String, String[]> entry : parameterMap.entrySet()) {
            String pname = entry.getKey();
            System.out.printf("%s: %s%n", pname, Arrays.toString(entry.getValue()));
        }

        String strLopnd = parameterMap.get(LEFT_OPERAND)[0];
        String strRopnd = parameterMap.get(RIGHT_OPERAND)[0];
        String strOprtr = parameterMap.get(OPERATOR)[0];

        double lopnd = parseParamOrThrow(strLopnd, "Left operand is not a number: %s");
        double ropnd = parseParamOrThrow(strRopnd, "Right operand is not a number: %s");

        BinaryOperator op = BinaryOperator.fromVerb(strOprtr);
        if (op == null) {
            throw new CalculatorParameterException("Unknown operator: "+strOprtr);
        }

        if (redis != null) {
            try {
                redis.set("loperand", strLopnd);
                redis.set("operator", strOprtr);
                redis.set("roperand", strRopnd);
            } catch (Exception e) {
                System.err.println("Something went wrong trying to cache");
                e.printStackTrace();
            }
        }

        return new BinaryCalculation(lopnd, ropnd, op);
    }

    private static double parseParamOrThrow(String param, String errMsgFmt) throws CalculatorParameterException {
        try {
            return Double.parseDouble(param);
        } catch (NumberFormatException e) {
            throw new CalculatorParameterException(String.format(errMsgFmt, param), e);
        }
    }

    private static void renderHtml(BinaryCalculation calc, PrintWriter writer) {
        writer.println("<html>");
        writer.println("<head><title>Calculation Result</title></head>");
        writer.println("<body>");
        writer.printf("<i>%s</i> %s <i>%s</i> = <b>%s</b>%n",
                calc.getLeftOperandFormatted(),
                calc.getOperatorSymbol(),
                calc.getRightOperandFormatted(),
                calc.resultFormatted());
        writer.println("<p><a href=\".\">Do Another Calculation</a></p>");
        writer.println("</body></html>");
    }

}
