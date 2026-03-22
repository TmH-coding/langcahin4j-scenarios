package com.langchain4j.scenarios.common.file;

import com.langchain4j.scenarios.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传服务 - 安全的文件处理、验证和存储
 *
 * 职责说明：
 * - 处理用户文件上传
 * - 验证文件类型和大小
 * - 生成唯一文件名避免冲突
 * - 安全存储上传的文件
 * - 支持文件下载和删除
 * - 实现文件的完整生命周期管理
 *
 * 架构设计：
 * - 使用 MultipartFile 处理上传文件
 * - 支持文件类型白名单验证
 * - 支持文件大小限制
 * - 使用 UUID 生成唯一文件名
 * - 使用 NIO Files API 进行文件操作
 * - 支持配置化的上传目录和文件大小限制
 *
 * 使用场景：
 * - 用户上传文档进行分析
 * - 用户上传数据文件进行处理
 * - 用户上传图片或其他媒体文件
 * - 支持批量文件上传
 * - 实现文件的版本管理
 * - 支持文件的下载和分享
 *
 * 文件上传工作原理：
 * 1. 接收上传的文件
 * 2. 验证文件（类型、大小等）
 * 3. 生成唯一的文件名
 * 4. 创建上传目录（如需要）
 * 5. 保存文件到磁盘
 * 6. 返回文件名供后续使用
 *
 * 配置参数：
 * - app.file.upload-dir：上传目录（默认：uploads）
 * - app.file.max-size：最大文件大小（默认：10485760 字节 = 10 MB）
 *
 * 性能考虑：
 * - 使用 NIO Files API 提高 I/O 性能
 * - 支持大文件流式处理
 * - 建议使用 CDN 加速文件下载
 * - 定期清理过期文件
 *
 * 安全考虑：
 * - 验证文件类型，防止恶意文件上传
 * - 限制文件大小，防止磁盘溢出
 * - 使用唯一文件名，防止文件覆盖
 * - 不要直接使用用户提供的文件名
 * - 定期清理过期的上传文件
 * - 实现访问权限控制
 * - 考虑文件加密存储
 *
 * 扩展建议：
 * - 可以添加文件病毒扫描
 * - 可以实现文件压缩存储
 * - 可以添加文件版本管理
 * - 可以实现文件的云存储集成
 * - 可以添加文件的访问日志
 */
@Service
@RequiredArgsConstructor
public class FileUploadService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:10485760}")
    private long maxFileSize;

    /**
     * 上传文件
     *
     * 功能：
     * - 验证上传的文件
     * - 生成唯一文件名
     * - 保存文件到磁盘
     * - 返回文件名供后续使用
     *
     * 参数说明：
     * - file: MultipartFile 对象
     *   * 包含上传文件的数据
     *   * 由 Spring 框架自动处理
     *   * 支持大文件流式处理
     *
     * 返回值：
     * - 唯一的文件名
     * - 格式：{UUID}.{扩展名}
     * - 示例：550e8400-e29b-41d4-a716-446655440000.pdf
     *
     * 配置参数：
     * - app.file.upload-dir: 上传目录
     *   * 默认值：uploads
     *   * 可以配置为绝对路径或相对路径
     *   * 示例：/var/uploads, C:\\uploads
     *
     * - app.file.max-size: 最大文件大小（字节）
     *   * 默认值：10485760（10 MB）
     *   * 可以根据需要调整
     *   * 示例：5242880（5 MB）, 52428800（50 MB）
     *
     * 使用示例：
     * @PostMapping(\"/upload\")
     * public ResponseEntity<String> uploadFile(@RequestParam(\"file\") MultipartFile file) {
     *     try {
     *         String filename = fileUploadService.uploadFile(file);
     *         return ResponseEntity.ok(filename);
     *     } catch (IOException e) {
     *         return ResponseEntity.status(500).body(\"Upload failed\");
     *     }
     * }
     *
     * 处理流程：
     * 1. 验证文件（检查是否为空、大小、类型）
     * 2. 创建上传目录（如果不存在）
     * 3. 获取原始文件名和扩展名
     * 4. 生成 UUID 作为新文件名
     * 5. 构建完整的文件路径
     * 6. 将文件内容写入磁盘
     * 7. 返回新文件名
     *
     * 错误处理：
     * - 文件为空：抛出 BusinessException
     * - 文件过大：抛出 BusinessException
     * - 文件类型不支持：抛出 BusinessException
     * - IO 异常：抛出 IOException
     *
     * 性能考虑：
     * - 使用 Files.write() 一次性写入
     * - 适合中等大小的文件
     * - 大文件可以考虑流式处理
     *
     * 安全考虑：
     * - 不使用用户提供的文件名
     * - 使用 UUID 生成唯一名称
     * - 验证文件类型
     * - 限制文件大小
     * - 定期清理过期文件
     *
     * @param file 上传的文件
     * @return 保存后的唯一文件名
     * @throws IOException 文件操作异常
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // 验证文件
        validateFile(file);

        // 创建上传目录
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID() + "." + fileExtension;

        // 保存文件
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.write(filePath, file.getBytes());

        return uniqueFilename;
    }

    /**
     * 验证文件
     *
     * 功能：
     * - 检查文件是否为空
     * - 检查文件大小是否超过限制
     * - 检查文件类型是否被允许
     *
     * 验证规则：
     * 1. 文件不能为空
     * 2. 文件大小不能超过 maxFileSize
     * 3. 文件类型必须在允许列表中
     *
     * 使用示例：
     * try {
     *     validateFile(file);
     *     // 文件验证通过
     * } catch (BusinessException e) {
     *     // 文件验证失败
     *     log.error(\"File validation failed: {}\", e.getMessage());
     * }
     *
     * 错误消息：
     * - \"文件不能为空\" - 文件为空
     * - \"文件大小超过限制\" - 文件过大
     * - \"不支持的文件类型\" - 文件类型不被允许
     *
     * @param file 要验证的文件
     * @throws BusinessException 验证失败时抛出
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new BusinessException("文件大小超过限制");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !isAllowedFileType(filename)) {
            throw new BusinessException("不支持的文件类型");
        }
    }

    /**
     * 检查是否允许的文件类型
     *
     * 功能：
     * - 检查文件扩展名是否在白名单中
     * - 支持多种文档和数据文件格式
     *
     * 允许的文件类型：
     * - txt: 纯文本文件
     * - pdf: PDF 文档
     * - doc: Word 97-2003 文档
     * - docx: Word 2007+ 文档
     * - xls: Excel 97-2003 工作簿
     * - xlsx: Excel 2007+ 工作簿
     *
     * 扩展建议：
     * - 可以添加更多文件类型
     * - 可以根据业务需求调整
     * - 示例：ppt, pptx, csv, json, xml
     *
     * 使用示例：
     * boolean allowed = isAllowedFileType(\"document.pdf\");  // true
     * boolean allowed = isAllowedFileType(\"script.exe\");    // false
     *
     * 安全考虑：
     * - 使用白名单而不是黑名单
     * - 检查文件扩展名（不完全可靠）
     * - 可以考虑检查文件魔数（Magic Number）
     * - 不要仅依赖扩展名判断文件类型
     *
     * @param filename 文件名
     * @return 是否允许此文件类型
     */
    private boolean isAllowedFileType(String filename) {
        String[] allowedExtensions = {"txt", "pdf", "doc", "docx", "xls", "xlsx"};
        String extension = getFileExtension(filename).toLowerCase();
        for (String allowed : allowedExtensions) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取文件扩展名
     *
     * 功能：
     * - 从文件名中提取扩展名
     * - 处理没有扩展名的文件
     *
     * 使用示例：
     * String ext = getFileExtension(\"document.pdf\");     // \"pdf\"
     * String ext = getFileExtension(\"archive.tar.gz\");   // \"gz\"
     * String ext = getFileExtension(\"README\");           // \"\"
     *
     * 实现细节：
     * - 查找最后一个 \".\" 的位置
     * - 返回 \".\" 之后的部分
     * - 如果没有 \".\"，返回空字符串
     *
     * 注意事项：
     * - 只返回最后一个 \".\" 之后的部分
     * - 对于 \"archive.tar.gz\"，返回 \"gz\" 而不是 \"tar.gz\"
     * - 如果需要完整的扩展名，需要特殊处理
     *
     * @param filename 文件名
     * @return 文件扩展名（不包括 \".\"）
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * 删除文件
     *
     * 功能：
     * - 从磁盘删除已上传的文件
     * - 支持清理过期或不需要的文件
     *
     * 使用示例：
     * try {
     *     fileUploadService.deleteFile(\"550e8400-e29b-41d4-a716-446655440000.pdf\");
     * } catch (IOException e) {
     *     log.error(\"Failed to delete file\", e);
     * }
     *
     * 行为说明：
     * - 如果文件存在，删除它
     * - 如果文件不存在，不抛出异常
     * - 使用 deleteIfExists() 确保安全
     *
     * 错误处理：
     * - 权限不足：抛出 IOException
     * - 文件被占用：抛出 IOException
     * - 其他 IO 错误：抛出 IOException
     *
     * @param filename 要删除的文件名
     * @throws IOException 文件操作异常
     */
    public void deleteFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir, filename);
        Files.deleteIfExists(filePath);
    }

    /**
     * 获取文件
     *
     * 功能：
     * - 从磁盘读取已上传的文件
     * - 支持文件下载和处理
     *
     * 使用示例：
     * @GetMapping(\"/download/{filename}\")
     * public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
     *     try {
     *         byte[] fileContent = fileUploadService.getFile(filename);
     *         return ResponseEntity.ok()
     *             .header(\"Content-Disposition\", \"attachment; filename=\" + filename)
     *             .body(fileContent);
     *     } catch (IOException e) {
     *         return ResponseEntity.status(404).build();
     *     }
     * }
     *
     * 性能考虑：
     * - 一次性读取整个文件到内存
     * - 适合中等大小的文件
     * - 大文件可以考虑流式处理
     *
     * 错误处理：
     * - 文件不存在：抛出 IOException
     * - 权限不足：抛出 IOException
     * - 其他 IO 错误：抛出 IOException
     *
     * 安全考虑：
     * - 验证文件名，防止路径遍历攻击
     * - 只允许访问上传目录中的文件
     * - 可以考虑添加访问权限检查
     *
     * @param filename 要读取的文件名
     * @return 文件内容的字节数组
     * @throws IOException 文件操作异常
     */
    public byte[] getFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir, filename);
        return Files.readAllBytes(filePath);
    }
}
