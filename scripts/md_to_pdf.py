#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import argparse
import io
import os
import sys

try:
    import markdown
except Exception:
    print('Missing python package: markdown', file=sys.stderr)
    raise

try:
    from xhtml2pdf import pisa
except Exception:
    print('Missing python package: xhtml2pdf', file=sys.stderr)
    raise

TEMPLATE = '''
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<style>
@font-face {
    font-family: 'MSYH';
    src: url('file:///C:/Windows/Fonts/msyh.ttf');
}
@font-face {
    font-family: 'SimSun';
    src: url('file:///C:/Windows/Fonts/simsun.ttc');
}
body { font-family: MSYH, SimSun, DejaVu Sans, Arial, Helvetica, sans-serif; padding: 20px; color: #111; }
h1,h2,h3,h4 { color: #111 }
pre { background: #f6f8fa; padding: 10px; border-radius: 4px }
table { border-collapse: collapse }
td,th { border: 1px solid #ccc; padding: 6px }
</style>
</head>
<body>
{content}
</body>
</html>
'''


def md_to_pdf(input_path: str, output_path: str) -> int:
    with open(input_path, 'r', encoding='utf-8') as f:
        md = f.read()
    html_body = markdown.markdown(md, extensions=['fenced_code', 'tables'])
    # Use replace instead of str.format to avoid accidental replacement
    # from braces inside CSS or content
    html = TEMPLATE.replace('{content}', html_body)

    # xhtml2pdf expects bytes/stream
    # Write to a temporary file first to avoid permission issues if the
    # target PDF is currently open in a viewer. Then atomically replace.
    tmp_path = output_path + '.tmp'
    with open(tmp_path, 'wb') as f:
        result = pisa.CreatePDF(io.BytesIO(html.encode('utf-8')), dest=f)
    # Replace target file
    try:
        os.replace(tmp_path, output_path)
    except PermissionError:
        # If replace fails due to file lock, raise with info
        raise
    return result.err


def main():
    parser = argparse.ArgumentParser(description='Convert Markdown to PDF (simple).')
    parser.add_argument('input', help='Input markdown file')
    parser.add_argument('output', nargs='?', help='Output PDF file (optional)')
    args = parser.parse_args()

    input_path = args.input
    if not args.output:
        base = os.path.splitext(os.path.basename(input_path))[0]
        output_path = os.path.join(os.path.dirname(input_path), base + '.pdf')
    else:
        output_path = args.output

    print(f'Converting: {input_path} -> {output_path}')
    err = md_to_pdf(input_path, output_path)
    if err:
        print('PDF generation failed with errors', file=sys.stderr)
        sys.exit(1)
    print('PDF generated successfully')

if __name__ == '__main__':
    main()
