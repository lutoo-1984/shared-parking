#!/usr/bin/env python3
# -*- coding: utf-8 -*-
import os
import sys
import argparse

from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.pdfbase import ttfonts
from reportlab.pdfbase import pdfmetrics


def md_to_paragraphs(md_text):
    # Very simple split: paragraphs by blank lines
    paras = [p.strip() for p in md_text.split('\n\n') if p.strip()]
    return paras


def generate(input_path, output_path):
    with open(input_path, 'r', encoding='utf-8') as f:
        md = f.read()
    paras = md_to_paragraphs(md)

    # Register Chinese font (Microsoft YaHei) if available
    font_paths = [
        r'C:\Windows\Fonts\msyh.ttf',
        r'C:\Windows\Fonts\simsun.ttc',
        r'C:\Windows\Fonts\simhei.ttf'
    ]
    font_name = None
    for p in font_paths:
        if os.path.exists(p):
            try:
                face = ttfonts.TTFont('MSYH', p)
                pdfmetrics.registerFont(face)
                font_name = 'MSYH'
                break
            except Exception:
                continue

    if font_name is None:
        # fallback to Helvetica
        font_name = 'Helvetica'

    doc = SimpleDocTemplate(output_path, pagesize=A4,
                            rightMargin=40,leftMargin=40,
                            topMargin=40,bottomMargin=40)
    styles = getSampleStyleSheet()
    normal = ParagraphStyle('normal', parent=styles['Normal'], fontName=font_name, fontSize=11, leading=14)
    story = []
    for p in paras:
        # Escape special characters minimally for Paragraph
        p = p.replace('&','&amp;').replace('<','&lt;').replace('>','&gt;')
        story.append(Paragraph(p.replace('\n','<br/>'), normal))
        story.append(Spacer(1,8))
    doc.build(story)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('input')
    parser.add_argument('output', nargs='?', default=None)
    args = parser.parse_args()
    inp = args.input
    out = args.output or os.path.splitext(inp)[0] + '_reportlab.pdf'
    print('Generating with reportlab:', inp, '->', out)
    generate(inp, out)
    print('Done')
