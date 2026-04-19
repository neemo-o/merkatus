import PropTypes from "prop-types";

export default function Badge({
  children,
  variant = "default",
  size = "sm",
  className = "",
}) {
  const variantClasses = {
    default: "bg-[var(--surface)] text-[var(--muted)] border-[var(--border)]",
    success: "bg-green-500/10 text-green-400 border-green-500/30",
    warning: "bg-yellow-500/10 text-yellow-400 border-yellow-500/30",
    error: "bg-red-500/10 text-red-400 border-red-500/30",
    info: "bg-blue-500/10 text-blue-400 border-blue-500/30",
  };

  const sizeClasses = {
    sm: "px-2 py-1 text-xs",
    md: "px-3 py-1.5 text-sm",
  };

  const classes = `inline-flex items-center rounded-full border font-medium ${variantClasses[variant]} ${sizeClasses[size]} ${className}`;

  return <span className={classes}>{children}</span>;
}

Badge.propTypes = {
  children: PropTypes.node.isRequired,
  variant: PropTypes.oneOf(["default", "success", "warning", "error", "info"]),
  size: PropTypes.oneOf(["sm", "md"]),
  className: PropTypes.string,
};
