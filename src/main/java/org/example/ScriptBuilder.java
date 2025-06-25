package org.example;

import java.util.Map;

/**
 * ScriptBuilder is responsible for assembling the final K6 script output.
 * Combines test groups using the "group()" function into a single default test function.
 * Assembles the final K6 script from grouped test blocks using group() calls only.
 */
public class ScriptBuilder {

    public String buildFullScript(Map<String, StringBuilder> groupedBlocks, String baseUrl, String token, String company) {
        StringBuilder script = new StringBuilder();

        script.append("""
            import http from 'k6/http';
            import { check, sleep, group } from 'k6';
            import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';

            export const options = { vus: 1, duration: '3s' };

            """);

        script.append(String.format("""
            const BASE_URL = '%s';
            const TOKEN = '%s';
            const COMPANY = '%s';

            const HEADERS = {
                'Authorization': `Bearer ${TOKEN}`,
                'Content-Type': 'application/json'
            };

            const TARGET_GROUP = __ENV.GROUP_NAME;

            export default function () {
        """, baseUrl, token, company));

        for (Map.Entry<String, StringBuilder> entry : groupedBlocks.entrySet()) {
            String tag = entry.getKey();
            script.append(String.format("""
                if (!TARGET_GROUP || TARGET_GROUP === '%s') {
                    group('%s', () => {
                        %s
                    });
                }

                """, tag, tag, entry.getValue().toString()));
        }

        script.append("""
            }

            export function handleSummary(data) {
                return {
                    'stdout': textSummary(data, { indent: ' ', enableColors: true }),
                };
            }
        """);

        return script.toString();
    }
}